package au.com.integradev.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.FieldSectionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "EmptyFieldSectionRule", repositoryKey = "delph")
@Rule(key = "EmptyFieldSection")
public class EmptyFieldSectionCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this empty field section.";

  @Override
  public DelphiCheckContext visit(FieldSectionNode fieldSection, DelphiCheckContext context) {
    if (fieldSection.getDeclarations().isEmpty()) {
      reportIssue(context, fieldSection, MESSAGE);
    }
    return super.visit(fieldSection, context);
  }
}
