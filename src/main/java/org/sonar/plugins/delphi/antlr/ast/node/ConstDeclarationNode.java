package org.sonar.plugins.delphi.antlr.ast.node;

import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public final class ConstDeclarationNode extends DelphiNode implements Typed, Visibility {
  public ConstDeclarationNode(Token token) {
    super(token);
  }

  public ConstDeclarationNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public NameDeclarationNode getNameDeclarationNode() {
    return (NameDeclarationNode) jjtGetChild(0);
  }

  public ExpressionNode getExpression() {
    return (ExpressionNode) jjtGetChild(1);
  }

  public TypeNode getTypeNode() {
    Node typeNode = jjtGetChild(2);
    return (typeNode instanceof TypeNode) ? (TypeNode) typeNode : null;
  }

  @Override
  @NotNull
  public Type getType() {
    TypeNode typeNode = getTypeNode();
    if (typeNode != null) {
      return typeNode.getType();
    }
    return getExpression().getType();
  }

  @Override
  public VisibilityType getVisibility() {
    Node parent = jjtGetParent();
    if (parent instanceof ConstSectionNode) {
      return ((ConstSectionNode) parent).getVisibility();
    }
    return VisibilityType.PUBLIC;
  }
}
