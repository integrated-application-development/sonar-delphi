package au.com.integradev.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.WithStatementNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "AvoidWithRule", repositoryKey = "delph")
@Rule(key = "WithStatement")
public class WithStatementCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this usage of 'with'.";

  @Override
  public DelphiCheckContext visit(WithStatementNode withStatement, DelphiCheckContext context) {
    context
        .newIssue()
        .onFilePosition(FilePosition.from(withStatement.getToken()))
        .withMessage(MESSAGE)
        .report();

    return super.visit(withStatement, context);
  }
}
