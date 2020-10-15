package org.sonar.plugins.delphi.symbol.resolve;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.LiteralNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Typed;

public class InvocationArgument implements Typed {
  private Type type = DelphiType.unknownType();
  private ExpressionNode expression;

  // Null if the expression was not a PrimaryExpression.
  // This is important because method references can only be bare PrimaryExpressions.
  @Nullable private NameResolver resolver;

  InvocationArgument(ExpressionNode expression) {
    this.expression = expression;

    if (expression instanceof PrimaryExpressionNode) {
      resolver = new NameResolver();
      resolver.readPrimaryExpression((PrimaryExpressionNode) expression);
      type = resolver.getApproximateType();
    }

    if (type.isUnknown()) {
      type = expression.getType();
    }
  }

  void resolve(Type parameterType) {
    if (resolver != null) {
      if (isMethodReference(parameterType)) {
        disambiguateMethodReference(resolver, parameterType);
      } else if (!resolver.isExplicitInvocation()) {
        resolver.disambiguateImplicitEmptyArgumentList();
      }
      resolver.addToSymbolTable();
      type = resolver.getApproximateType();
    }
  }

  boolean looksLikeMethodReference() {
    return resolver != null && !resolver.isExplicitInvocation() && type.isProcedural();
  }

  boolean isMethodReference(Type parameterType) {
    return resolver != null
        && !resolver.isExplicitInvocation()
        && type.isProcedural()
        && parameterType.isProcedural();
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
