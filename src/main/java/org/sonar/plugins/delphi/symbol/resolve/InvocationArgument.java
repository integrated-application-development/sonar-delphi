package org.sonar.plugins.delphi.symbol.resolve;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.LiteralNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnaryExpressionNode;
import org.sonar.plugins.delphi.operator.UnaryOperator;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Typed;

public class InvocationArgument implements Typed {
  private final ExpressionNode expression;
  private Type type;
  @Nullable private NameResolver resolver;

  InvocationArgument(ExpressionNode expression) {
    this.expression = expression;
    this.type = null;

    if (expression instanceof UnaryExpressionNode) {
      this.initializeFromAddressOfProcedural((UnaryExpressionNode) expression);
    } else if (expression instanceof PrimaryExpressionNode) {
      this.initializeFromPrimaryExpression((PrimaryExpressionNode) expression);
    }

    if (this.type == null) {
      this.type = expression.getType();
    }
  }

  private void initializeFromAddressOfProcedural(UnaryExpressionNode unary) {
    if (unary.getOperator() != UnaryOperator.ADDRESS) {
      return;
    }

    if (!(unary.getOperand() instanceof PrimaryExpressionNode)) {
      return;
    }

    NameResolver nameResolver = new NameResolver(unary.getTypeFactory());
    nameResolver.readPrimaryExpression((PrimaryExpressionNode) unary.getOperand());

    Type approximateType = nameResolver.getApproximateType();
    if (!nameResolver.isExplicitInvocation() && approximateType.isProcedural()) {
      this.resolver = nameResolver;
      this.type = approximateType;
    }
  }

  private void initializeFromPrimaryExpression(PrimaryExpressionNode primary) {
    this.resolver = new NameResolver(primary.getTypeFactory());
    resolver.readPrimaryExpression(primary);
    this.type = resolver.getApproximateType();
  }

  void resolve(Type parameterType) {
    if (resolver != null) {
      if (isMethodReference(parameterType)) {
        disambiguateMethodReference(resolver, parameterType);
      } else if (!resolver.isExplicitInvocation()) {
        resolver.disambiguateImplicitEmptyArgumentList();
      }
      resolver.addToSymbolTable();
    }
  }

  boolean looksLikeProceduralReference() {
    return resolver != null
        && !resolver.isExplicitInvocation()
        && resolver.getApproximateType().isProcedural();
  }

  boolean isMethodReference(Type parameterType) {
    return resolver != null
        && !resolver.isExplicitInvocation()
        && resolver.getApproximateType().isMethod()
        && parameterType.isProcedural();
  }

  boolean violatesBounds(Type type) {
    return BoundsChecker.forType(type).violatesBounds(expression);
  }

  boolean isImplicitlyConvertibleToNilPointer() {
    LiteralNode literal = expression.extractLiteral();
    return literal != null
        && (literal.isIntegerLiteral() || literal.isHexadecimalLiteral())
        && literal.getValueAsInt() == 0;
  }

  Type findMethodReferenceType(Type parameterType) {
    checkArgument(parameterType instanceof ProceduralType);
    checkNotNull(resolver);

    NameResolver clone = new NameResolver(resolver);
    disambiguateMethodReference(clone, parameterType);
    return resolver.getApproximateType();
  }

  private static void disambiguateMethodReference(NameResolver resolver, Type parameterType) {
    resolver.disambiguateMethodReference((ProceduralType) parameterType);
    resolver.checkAmbiguity();
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }
}
