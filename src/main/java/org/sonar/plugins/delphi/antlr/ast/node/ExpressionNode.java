package org.sonar.plugins.delphi.antlr.ast.node;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public abstract class ExpressionNode extends DelphiNode implements Typed {
  private Type type;

  protected ExpressionNode(Token token) {
    super(token);
  }

  protected ExpressionNode(int tokenType) {
    super(tokenType);
  }

  @Override
  @NotNull
  public Type getType() {
    if (type == null) {
      type = createType();
    }
    return type;
  }

  @Override
  public abstract String getImage();

  @NotNull
  protected abstract Type createType();

  public boolean isLiteral() {
    return extractLiteral() != null;
  }

  public boolean isIntegerLiteral() {
    LiteralNode literal = extractLiteral();
    return literal != null && literal.isIntegerLiteral();
  }

  public boolean isHexadecimalLiteral() {
    LiteralNode literal = extractLiteral();
    return literal != null && literal.isHexadecimalLiteral();
  }

  public boolean isBinaryLiteral() {
    LiteralNode literal = extractLiteral();
    return literal != null && literal.isBinaryLiteral();
  }

  public boolean isStringLiteral() {
    LiteralNode literal = extractLiteral();
    return literal != null && literal.isTextLiteral();
  }

  public boolean isRealLiteral() {
    LiteralNode literal = extractLiteral();
    return literal != null && literal.isDecimalLiteral();
  }

  public boolean isNilLiteral() {
    LiteralNode literal = extractLiteral();
    return literal != null && literal.isNilLiteral();
  }

  public boolean isBooleanLiteral() {
    return isTrue() || isFalse();
  }

  public boolean isTrue() {
    return isReferenceTo("True");
  }

  public boolean isFalse() {
    return isReferenceTo("False");
  }

  public boolean isResult() {
    return isReferenceTo("Result");
  }

  private boolean isReferenceTo(String image) {
    ExpressionNode expression = skipParentheses();
    if (expression.jjtGetNumChildren() == 1 && expression instanceof PrimaryExpressionNode) {
      Node child = expression.jjtGetChild(0);
      if (child instanceof NameReferenceNode) {
        NameReferenceNode reference = (NameReferenceNode) child;
        NameDeclaration declaration = reference.getLastName().getNameDeclaration();
        return declaration != null && declaration.getImage().equals(image);
      }
    }
    return false;
  }

  @Nullable
  public LiteralNode extractLiteral() {
    ExpressionNode expression = skipParentheses();
    if (expression instanceof PrimaryExpressionNode) {
      PrimaryExpressionNode primary = (PrimaryExpressionNode) expression;
      if (primary.jjtGetNumChildren() == 1 && primary.jjtGetChild(0) instanceof LiteralNode) {
        return (LiteralNode) primary.jjtGetChild(0);
      }
    }
    return null;
  }

  @Nullable
  public NameReferenceNode extractSimpleNameReference() {
    ExpressionNode expression = skipParentheses();
    if (expression instanceof PrimaryExpressionNode && expression.jjtGetNumChildren() == 1) {
      Node child = expression.jjtGetChild(0);
      if (child instanceof NameReferenceNode) {
        NameReferenceNode nameReference = (NameReferenceNode) child;
        if (nameReference.nextName() == null) {
          return nameReference;
        }
      }
    }
    return null;
  }

  /**
   * Traverses nested parenthesized expressions and returns the expression inside of them
   *
   * @return Nested expression
   */
  @NotNull
  public ExpressionNode skipParentheses() {
    ExpressionNode result = this;
    while (result instanceof ParenthesizedExpressionNode) {
      result = ((ParenthesizedExpressionNode) result).getExpression();
    }
    return result;
  }

  /**
   * From a potentially nested expression, traverses any parenthesized expressions ancestors and
   * returns the top-level expression. The reverse of {@link ExpressionNode#skipParentheses}
   *
   * @return Top-level expression
   */
  @NotNull
  public ExpressionNode findParentheses() {
    ExpressionNode result = this;
    while (result.jjtGetParent() instanceof ParenthesizedExpressionNode) {
      result = (ExpressionNode) result.jjtGetParent();
    }
    return result;
  }
}
