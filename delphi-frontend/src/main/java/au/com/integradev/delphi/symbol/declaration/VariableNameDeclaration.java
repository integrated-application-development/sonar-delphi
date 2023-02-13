/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.symbol.declaration;

import org.sonar.plugins.communitydelphi.api.ast.ConstStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.FieldSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.ForInStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.ForToStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode.DeclarationKind;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.RecordVariantItemNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.ast.VarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.VarStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.Visibility;
import au.com.integradev.delphi.symbol.NameDeclaration;
import au.com.integradev.delphi.symbol.SymbolicNode;
import au.com.integradev.delphi.symbol.resolve.TypeInferrer;
import au.com.integradev.delphi.symbol.scope.DelphiScope;
import au.com.integradev.delphi.type.DelphiType;
import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Type.ArrayConstructorType;
import au.com.integradev.delphi.type.Typed;
import au.com.integradev.delphi.type.factory.TypeFactory;
import au.com.integradev.delphi.type.generic.TypeSpecializationContext;
import au.com.integradev.delphi.type.intrinsic.IntrinsicType;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class VariableNameDeclaration extends AbstractNameDeclaration
    implements TypedDeclaration, Visibility {
  private final Type type;
  private final VisibilityType visibility;
  private final boolean inline;
  private final boolean field;
  private final boolean classVar;
  private final boolean union;
  private int hashCode;

  public VariableNameDeclaration(NameDeclarationNode node) {
    this(
        new SymbolicNode(node),
        extractType(node),
        extractVisibility(node),
        extractInline(node),
        extractField(node),
        extractClassVar(node),
        extractUnion(node));
  }

  private VariableNameDeclaration(String image, Type type, DelphiScope scope) {
    super(SymbolicNode.imaginary(image, scope));
    this.type = type;
    this.visibility = VisibilityType.PUBLIC;
    this.inline = false;
    this.field = false;
    this.classVar = false;
    this.union = false;
  }

  private VariableNameDeclaration(
      SymbolicNode location,
      Type type,
      VisibilityType visibility,
      boolean inline,
      boolean field,
      boolean classVar,
      boolean union) {
    super(location);
    this.type = type;
    this.visibility = visibility;
    this.inline = inline;
    this.field = field;
    this.classVar = classVar;
    this.union = union;
  }

  public static VariableNameDeclaration compilerVariable(
      String image, Type type, DelphiScope scope) {
    return new VariableNameDeclaration(image, type, scope);
  }

  private static Type extractType(NameDeclarationNode node) {
    switch (node.getKind()) {
      case CONST:
        return constType(node);
      case EXCEPT_ITEM:
      case RECORD_VARIANT_TAG:
        return ((Typed) node.jjtGetParent()).getType();
      case PARAMETER:
      case FIELD:
      case VAR:
        return ((Typed) node.getNthParent(2)).getType();
      case INLINE_CONST:
        return inlineConstType(node);
      case INLINE_VAR:
        return inlineVarType(node);
      case LOOP_VAR:
        return loopVarType(node);
      default:
        throw new AssertionError("Unhandled DeclarationKind");
    }
  }

  private static Type constType(NameDeclarationNode node) {
    Type type = ((Typed) node.jjtGetParent()).getType();
    if (type.isArrayConstructor()) {
      List<Type> elementTypes = ((ArrayConstructorType) type).elementTypes();
      Type elementType = elementTypes.stream().findFirst().orElse(DelphiType.voidType());
      TypeFactory typeFactory = node.getTypeFactory();
      if (elementType.isInteger()) {
        elementType = typeFactory.getIntrinsic(IntrinsicType.BYTE);
      } else if (elementType.isChar()) {
        elementType = typeFactory.getIntrinsic(IntrinsicType.ANSICHAR);
      }
      type = typeFactory.set(elementType);
    }
    return type;
  }

  private static Type inlineConstType(NameDeclarationNode node) {
    var constStatement = (ConstStatementNode) node.jjtGetParent();
    return getDeclaredTypeWithTypeInferenceFallback(
        node.getTypeFactory(), constStatement.getTypeNode(), constStatement.getExpression());
  }

  private static Type inlineVarType(NameDeclarationNode node) {
    var varStatement = (VarStatementNode) node.getNthParent(2);
    return getDeclaredTypeWithTypeInferenceFallback(
        node.getTypeFactory(), varStatement.getTypeNode(), varStatement.getExpression());
  }

  private static Type loopVarType(NameDeclarationNode node) {
    ForLoopVarDeclarationNode loopVarDeclaration = (ForLoopVarDeclarationNode) node.jjtGetParent();
    Node loop = loopVarDeclaration.jjtGetParent();

    Typed typed;
    if (loop instanceof ForToStatementNode) {
      typed = ((ForToStatementNode) loop).getInitializerExpression();
    } else {
      typed = ((ForInStatementNode) loop).getCurrentDeclaration();
    }

    return getDeclaredTypeWithTypeInferenceFallback(
        node.getTypeFactory(), loopVarDeclaration.getTypeNode(), typed);
  }

  private static Type getDeclaredTypeWithTypeInferenceFallback(
      TypeFactory typeFactory, TypeNode typeNode, Typed typed) {
    if (typeNode != null) {
      return typeNode.getType();
    } else {
      TypeInferrer inferrer = new TypeInferrer(typeFactory);
      return inferrer.infer(typed);
    }
  }

  private static VisibilityType extractVisibility(NameDeclarationNode node) {
    Visibility visibility;

    switch (node.getKind()) {
      case CONST:
        visibility = (Visibility) node.jjtGetParent();
        break;
      case FIELD:
        visibility = (Visibility) node.getNthParent(2);
        break;
      default:
        return VisibilityType.PUBLIC;
    }

    return visibility.getVisibility();
  }

  private static boolean extractInline(NameDeclarationNode node) {
    switch (node.getKind()) {
      case INLINE_CONST:
      case INLINE_VAR:
      case LOOP_VAR:
        return true;
      default:
        return false;
    }
  }

  private static boolean extractField(NameDeclarationNode node) {
    return node.getKind() == DeclarationKind.FIELD;
  }

  private static boolean extractClassVar(NameDeclarationNode node) {
    if (node.getKind() == DeclarationKind.FIELD) {
      Node ancestor = node.getNthParent(3);
      return ancestor instanceof FieldSectionNode
          && ((FieldSectionNode) ancestor).isClassFieldSection();
    }
    return false;
  }

  private static boolean extractUnion(NameDeclarationNode node) {
    switch (node.getKind()) {
      case FIELD:
        return node.getNthParent(3) instanceof RecordVariantItemNode;
      case VAR:
        return ((VarDeclarationNode) node.getNthParent(2)).isAbsolute();
      default:
        return false;
    }
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }

  @Override
  public VisibilityType getVisibility() {
    return visibility;
  }

  public boolean isInline() {
    return inline;
  }

  public boolean isField() {
    return field;
  }

  public boolean isClassVariable() {
    return classVar;
  }

  public boolean isUnion() {
    return union;
  }

  @Override
  protected NameDeclaration doSpecialization(TypeSpecializationContext context) {
    return new VariableNameDeclaration(
        node, type.specialize(context), visibility, inline, field, classVar, union);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof VariableNameDeclaration)) {
      return false;
    }
    VariableNameDeclaration that = (VariableNameDeclaration) other;
    return getImage().equalsIgnoreCase(that.getImage()) && type.is(that.type);
  }

  @Override
  public int hashCode() {
    if (hashCode == 0) {
      hashCode = Objects.hash(getImage().toLowerCase(), type.getImage());
    }
    return hashCode;
  }

  @Override
  public String toString() {
    return "Variable: image = '" + getImage();
  }
}
