package org.sonar.plugins.delphi.antlr.preprocessor.directive;

import static org.sonar.plugins.delphi.antlr.preprocessor.directive.CompilerDirective.Expression.ConstExpressionType.BOOLEAN;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.preprocessor.DelphiPreprocessor;
import org.sonar.plugins.delphi.antlr.preprocessor.directive.CompilerDirective.Expression.ExpressionValue;

public class IfDirective extends BranchDirective {
  private final CompilerDirective.Expression expression;

  IfDirective(Token token, CompilerDirectiveType type, CompilerDirective.Expression expression) {
    super(token, type);
    this.expression = expression;
  }

  @Override
  boolean isSuccessfulBranch(DelphiPreprocessor preprocessor) {
    ExpressionValue value = expression.evaluate(preprocessor);
    return value.type() == BOOLEAN && value.asBoolean();
  }
}
