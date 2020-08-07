package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.token.DelphiToken;
import org.sonar.plugins.delphi.preprocessor.directive.CompilerDirective;
import org.sonar.plugins.delphi.preprocessor.directive.HintsDirective;

public class CompilerHintsRule extends AbstractDelphiRule {
  @Override
  public void visitToken(DelphiToken token, RuleContext context) {
    if (token.isCompilerDirective()) {
      CompilerDirective directive = CompilerDirective.fromToken(token.getAntlrToken());
      if (directive instanceof HintsDirective && ((HintsDirective) directive).isOff()) {
        addViolation(context, token);
      }
    }
  }
}
