package org.sonar.plugins.delphi.symbol.resolve;

import com.google.common.base.Preconditions;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.symbol.ParameterDeclaration;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Typed;

class InvocationArgument implements Typed {

  private Type type = DelphiType.unknownType();

  // Null if the expression was not a PrimaryExpression.
  // This is important because method references can only be bare PrimaryExpressions.
  @Nullable private NameResolver resolver;

  public InvocationArgument(ExpressionNode argument) {
    if (argument instanceof PrimaryExpressionNode) {
      resolver = new NameResolver();
      resolver.readPrimaryExpression((PrimaryExpressionNode) argument);
      type = resolver.getApproximateType();
    }

    if (type == DelphiType.unknownType()) {
      type = argument.getType();
    }
  }

  public void resolve(ParameterDeclaration parameter) {
    if (resolver != null) {
      if (isMethodReference(parameter)) {
        disambiguateMethodReference(resolver, parameter);
      } else if (!resolver.isExplicitInvocation()) {
        resolver.disambiguateImplicitEmptyArgumentList();
      }
      resolver.addToSymbolTable();
    }
  }

  public boolean looksLikeMethodReference() {
    return resolver != null && !resolver.isExplicitInvocation() && type.isProcedural();
  }

  public boolean isMethodReference(Typed parameter) {
    return resolver != null
        && !resolver.isExplicitInvocation()
        && type.isMethod()
        && parameter.getType().isProcedural();
  }

  public Type findMethodReferenceType(ParameterDeclaration parameter) {
    Preconditions.checkArgument(parameter.getType() instanceof ProceduralType);
    Preconditions.checkNotNull(resolver);

    NameResolver clone = new NameResolver(resolver);
    disambiguateMethodReference(clone, parameter);
    return resolver.getApproximateType();
  }

  private static void disambiguateMethodReference(
      NameResolver resolver, ParameterDeclaration parameter) {
    resolver.disambiguateMethodReference((ProceduralType) parameter.getType());
    resolver.checkAmbiguity();
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }
}
