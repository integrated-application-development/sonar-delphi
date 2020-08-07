package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.token.DelphiToken;
import org.sonar.plugins.delphi.preprocessor.directive.CompilerDirective;
import org.sonar.plugins.delphi.preprocessor.directive.WarnDirective;
import org.sonar.plugins.delphi.preprocessor.directive.WarnDirective.WarnDirectiveValue;
import org.sonar.plugins.delphi.preprocessor.directive.WarningsDirective;

public class CompilerWarningsRule extends AbstractDelphiRule {
  @Override
  public void visitToken(DelphiToken token, RuleContext data) {
    if (token.isCompilerDirective()) {
      CompilerDirective directive = CompilerDirective.fromToken(token.getAntlrToken());
      if (isViolation(directive)) {
        addViolation(data, token);
      }
    }
  }

  private static boolean isViolation(CompilerDirective directive) {
    switch (directive.getType()) {
      case WARNINGS:
        return ((WarningsDirective) directive).isOff();
      case WARN:
        return ((WarnDirective) directive).getValue() == WarnDirectiveValue.OFF;
      default:
        return false;
    }
  }
}
