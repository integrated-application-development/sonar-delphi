package au.com.integradev.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.FinallyBlockNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "EmptyFinallyBlockRule", repositoryKey = "delph")
@Rule(key = "EmptyFinallyBlock")
public class EmptyFinallyBlockCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this empty 'finally' block.";

  @Override
  public DelphiCheckContext visit(FinallyBlockNode finallyBlock, DelphiCheckContext context) {
    if (finallyBlock.getStatementList().isEmpty()) {
      reportIssue(context, finallyBlock, MESSAGE);
    }
    return super.visit(finallyBlock, context);
  }
}
