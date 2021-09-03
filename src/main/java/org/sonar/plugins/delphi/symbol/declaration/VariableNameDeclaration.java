package org.sonar.plugins.delphi.symbol.declaration;

import java.util.List;
import java.util.Objects;
import net.sourceforge.pmd.lang.ast.Node;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.ConstStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForInStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForLoopVarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForToStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode.DeclarationKind;
import org.sonar.plugins.delphi.antlr.ast.node.RecordVariantItemNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.Visibility;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.resolve.TypeInferrer;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ArrayConstructorType;
import org.sonar.plugins.delphi.type.Typed;
import org.sonar.plugins.delphi.type.factory.TypeFactory;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicType;

public final class VariableNameDeclaration extends AbstractDelphiNameDeclaration
    implements TypedDeclaration, Visibility {
  private final Type type;
  private final VisibilityType visibility;
  private final boolean inline;
  private final boolean field;
  private final boolean union;
  private int hashCode;

  public VariableNameDeclaration(NameDeclarationNode node) {
    this(
        new SymbolicNode(node),
        extractType(node),
        extractVisibility(node),
        extractInline(node),
        extractField(node),
        extractUnion(node));
  }

  private VariableNameDeclaration(String image, Type type, DelphiScope scope) {
    super(SymbolicNode.imaginary(image, scope));
    this.type = type;
    this.visibility = VisibilityType.PUBLIC;
    this.inline = false;
    this.field = false;
    this.union = false;
  }

  private VariableNameDeclaration(
      SymbolicNode location,
      Type type,
      VisibilityType visibility,
      boolean inline,
      boolean field,
      boolean union) {
    super(location);
    this.type = type;
    this.visibility = visibility;
    this.inline = inline;
    this.field = field;
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

  public boolean isUnion() {
    return union;
  }

  @Override
  protected DelphiNameDeclaration doSpecialization(TypeSpecializationContext context) {
    return new VariableNameDeclaration(
        getNode(), type.specialize(context), visibility, inline, field, union);
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
