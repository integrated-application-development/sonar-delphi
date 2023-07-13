package au.com.integradev.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "ExtraneousArgumentListCommasRule", repositoryKey = "delph")
@Rule(key = "TrailingCommaArgumentList")
public class TrailingCommaArgumentListCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this trailing comma.";

  @Override
  public DelphiCheckContext visit(ArgumentListNode argumentList, DelphiCheckContext context) {
    DelphiNode node = argumentList.jjtGetChild(argumentList.jjtGetNumChildren() - 2);
    if (node.getTokenType() == DelphiTokenType.COMMA) {
      reportIssue(context, node, MESSAGE);
    }
    return super.visit(argumentList, context);
  }
}
