package au.com.integradev.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.GotoStatementNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "GotoStatementRule", repositoryKey = "delph")
@Rule(key = "GotoStatement")
public class GotoStatementCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this usage of 'goto'.";

  @Override
  public DelphiCheckContext visit(GotoStatementNode gotoStatement, DelphiCheckContext context) {
    reportIssue(context, gotoStatement, MESSAGE);
    return super.visit(gotoStatement, context);
  }
}
