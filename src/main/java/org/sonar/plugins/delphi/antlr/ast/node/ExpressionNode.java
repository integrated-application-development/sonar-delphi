package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public abstract class ExpressionNode extends DelphiNode implements Typed {
  private Type type;

  public ExpressionNode(Token token) {
    super(token);
  }

  public ExpressionNode(int tokenType) {
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

  public boolean isStringLiteral() {
    LiteralNode literal = extractLiteral();
    return literal != null && literal.isTextLiteral();
  }

  public boolean isRealLiteral() {
    LiteralNode literal = extractLiteral();
    return literal != null && literal.isDecimalLiteral();
  }

  public boolean isHexadecimalLiteral() {
    LiteralNode literal = extractLiteral();
    return literal != null && literal.isHexadecimalLiteral();
  }

  public boolean isNilLiteral() {
    LiteralNode literal = extractLiteral();
    return literal != null && literal.isNilLiteral();
  }

  public boolean isBooleanLiteral() {
    return isTrue() || isFalse();
  }

  public boolean isTrue() {
    ExpressionNode expr = skipParentheses();
    return expr.hasImageEqualTo("True") || expr.hasImageEqualTo("System.True");
  }

  public boolean isFalse() {
    ExpressionNode expr = skipParentheses();
    return expr.hasImageEqualTo("False") || expr.hasImageEqualTo("System.False");
  }

  public boolean isResult() {
    ExpressionNode expr = skipParentheses();
    return expr.hasImageEqualTo("Result");
  }

  @Nullable
  private LiteralNode extractLiteral() {
    ExpressionNode expr = skipParentheses();
    if (expr instanceof PrimaryExpressionNode) {
      PrimaryExpressionNode primary = (PrimaryExpressionNode) expr;
      if (primary.jjtGetNumChildren() == 1 && primary.jjtGetChild(0) instanceof LiteralNode) {
        return (LiteralNode) primary.jjtGetChild(0);
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
