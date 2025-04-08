/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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

import au.com.integradev.delphi.antlr.ast.node.DelphiNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.NameDeclarationNodeImpl;
import au.com.integradev.delphi.symbol.SymbolicNode;
import au.com.integradev.delphi.symbol.resolve.TypeInferrer;
import java.util.List;
import java.util.Objects;
import org.sonar.plugins.communitydelphi.api.ast.ConstStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.FieldSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.ForInStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.ForToStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.RecordVariantItemNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.ast.VarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.VarStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.Visibility;
import org.sonar.plugins.communitydelphi.api.symbol.EnumeratorOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ArrayConstructorType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;
import org.sonar.plugins.communitydelphi.api.type.TypeSpecializationContext;
import org.sonar.plugins.communitydelphi.api.type.Typed;

public final class VariableNameDeclarationImpl extends NameDeclarationImpl
    implements VariableNameDeclaration {
  private final Type type;
  private final VisibilityType visibility;
  private final Kind kind;
  private int hashCode;

  public VariableNameDeclarationImpl(NameDeclarationNode node) {
    this(new SymbolicNode(node), extractType(node), extractVisibility(node), extractKind(node));
  }

  private VariableNameDeclarationImpl(
      SymbolicNode location, Type type, VisibilityType visibility, Kind kind) {
    super(location);
    this.type = type;
    this.visibility = visibility;
    this.kind = kind;
  }

  public static VariableNameDeclaration result(Type type, DelphiScope scope) {
    return new VariableNameDeclarationImpl(
        SymbolicNode.imaginary("Result", scope), type, VisibilityType.PUBLIC, Kind.RESULT);
  }

  public static VariableNameDeclaration self(Type type, DelphiScope scope) {
    return new VariableNameDeclarationImpl(
        SymbolicNode.imaginary("Self", scope), type, VisibilityType.PUBLIC, Kind.SELF);
  }

  public static VariableNameDeclaration parameter(String name, Type type, DelphiScope scope) {
    return new VariableNameDeclarationImpl(
        SymbolicNode.imaginary(name, scope), type, VisibilityType.PUBLIC, Kind.PARAMETER);
  }

  public static VariableNameDeclaration constant(String name, Type type, DelphiScope scope) {
    return new VariableNameDeclarationImpl(
        SymbolicNode.imaginary(name, scope), type, VisibilityType.PUBLIC, Kind.CONST);
  }

  private static Type extractType(NameDeclarationNode node) {
    var nodeKind = ((NameDeclarationNodeImpl) node).getKind();
    switch (nodeKind) {
      case CONST:
        return constType(node);
      case EXCEPT_ITEM:
      case RECORD_VARIANT_TAG:
        return ((Typed) node.getParent()).getType();
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
    Type type = ((Typed) node.getParent()).getType();
    if (type.isArrayConstructor()) {
      List<Type> elementTypes = ((ArrayConstructorType) type).elementTypes();
      Type elementType = elementTypes.stream().findFirst().orElse(TypeFactory.voidType());
      TypeFactory typeFactory = ((DelphiNodeImpl) node).getTypeFactory();
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
    var constStatement = (ConstStatementNode) node.getParent();
    return getDeclaredTypeWithTypeInferenceFallback(
        ((DelphiNodeImpl) node).getTypeFactory(),
        constStatement.getTypeNode(),
        constStatement.getExpression());
  }

  private static Type inlineVarType(NameDeclarationNode node) {
    var varStatement = (VarStatementNode) node.getNthParent(2);
    return getDeclaredTypeWithTypeInferenceFallback(
        ((DelphiNodeImpl) node).getTypeFactory(),
        varStatement.getTypeNode(),
        varStatement.getExpression());
  }

  private static Type loopVarType(NameDeclarationNode node) {
    ForLoopVarDeclarationNode loopVarDeclaration = (ForLoopVarDeclarationNode) node.getParent();
    Node loop = loopVarDeclaration.getParent();

    Typed typed;
    if (loop instanceof ForToStatementNode) {
      typed = ((ForToStatementNode) loop).getInitializerExpression();
    } else {
      EnumeratorOccurrence enumerator = ((ForInStatementNode) loop).getEnumeratorOccurrence();
      if (enumerator != null) {
        typed = (Typed) enumerator.getCurrent().getNameDeclaration();
      } else {
        typed = null;
      }
    }

    return getDeclaredTypeWithTypeInferenceFallback(
        ((DelphiNodeImpl) node).getTypeFactory(), loopVarDeclaration.getTypeNode(), typed);
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
    var nodeKind = ((NameDeclarationNodeImpl) node).getKind();

    switch (nodeKind) {
      case CONST:
        visibility = (Visibility) node.getParent();
        break;
      case FIELD:
        visibility = (Visibility) node.getNthParent(2);
        break;
      default:
        return VisibilityType.PUBLIC;
    }

    return visibility.getVisibility();
  }

  private static Kind extractKind(NameDeclarationNode node) {
    var nodeKind = ((NameDeclarationNodeImpl) node).getKind();
    switch (nodeKind) {
      case CONST:
        return Kind.CONST;
      case EXCEPT_ITEM:
        return Kind.EXCEPT_ITEM;
      case FIELD:
        Node ancestor = node.getNthParent(3);
        if (ancestor instanceof FieldSectionNode
            && ((FieldSectionNode) ancestor).isClassFieldSection()) {
          return Kind.CLASS_VAR;
        }
        if (node.getNthParent(3) instanceof RecordVariantItemNode) {
          return Kind.RECORD_VARIANT_FIELD;
        }
        return Kind.FIELD;
      case INLINE_CONST:
        return Kind.INLINE_CONST;
      case INLINE_VAR:
      case LOOP_VAR:
        return Kind.INLINE_VAR;
      case PARAMETER:
        return Kind.PARAMETER;
      case RECORD_VARIANT_TAG:
        return Kind.RECORD_VARIANT_TAG;
      case VAR:
        if (((VarDeclarationNode) node.getNthParent(2)).isAbsolute()) {
          return Kind.ABSOLUTE_VAR;
        }
        return Kind.VAR;
      default:
        throw new AssertionError("Unhandled DeclarationKind");
    }
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public VisibilityType getVisibility() {
    return visibility;
  }

  @Override
  public boolean isInline() {
    switch (kind) {
      case INLINE_VAR:
      case INLINE_CONST:
        return true;
      default:
        return false;
    }
  }

  @Override
  public boolean isVar() {
    switch (kind) {
      case VAR:
      case INLINE_VAR:
      case ABSOLUTE_VAR:
        return true;
      default:
        return false;
    }
  }

  @Override
  public boolean isConst() {
    switch (kind) {
      case CONST:
      case INLINE_CONST:
        return true;
      default:
        return false;
    }
  }

  @Override
  public boolean isField() {
    switch (kind) {
      case FIELD:
      case RECORD_VARIANT_FIELD:
        return true;
      default:
        return false;
    }
  }

  @Override
  public boolean isClassVar() {
    return kind == Kind.CLASS_VAR;
  }

  @Override
  public boolean isUnion() {
    switch (kind) {
      case ABSOLUTE_VAR:
      case RECORD_VARIANT_FIELD:
        return true;
      default:
        return false;
    }
  }

  @Override
  public boolean isSelf() {
    return kind == Kind.SELF;
  }

  @Override
  public boolean isResult() {
    return kind == Kind.RESULT;
  }

  @Override
  protected NameDeclaration doSpecialization(TypeSpecializationContext context) {
    return new VariableNameDeclarationImpl(node, type.specialize(context), visibility, kind);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof VariableNameDeclarationImpl)) {
      return false;
    }
    VariableNameDeclarationImpl that = (VariableNameDeclarationImpl) other;
    return getImage().equalsIgnoreCase(that.getImage()) && type.is(that.type) && kind == that.kind;
  }

  @Override
  public int hashCode() {
    if (hashCode == 0) {
      hashCode = Objects.hash(getImage().toLowerCase(), type.getImage(), kind);
    }
    return hashCode;
  }

  @Override
  public String toString() {
    return "Variable: image = '" + getImage();
  }

  private enum Kind {
    VAR,
    CLASS_VAR,
    ABSOLUTE_VAR,
    CONST,
    FIELD,
    RECORD_VARIANT_FIELD,
    RECORD_VARIANT_TAG,
    EXCEPT_ITEM,
    INLINE_CONST,
    INLINE_VAR,
    PARAMETER,
    SELF,
    RESULT
  }
}
