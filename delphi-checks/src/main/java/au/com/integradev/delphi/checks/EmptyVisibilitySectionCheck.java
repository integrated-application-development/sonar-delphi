package au.com.integradev.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.VisibilitySectionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "EmptyVisibilitySectionRule", repositoryKey = "delph")
@Rule(key = "EmptyVisibilitySection")
public class EmptyVisibilitySectionCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this empty visibility section.";

  @Override
  public DelphiCheckContext visit(
      VisibilitySectionNode visibilitySection, DelphiCheckContext context) {
    if (!visibilitySection.isImplicitPublished() && visibilitySection.getChildrenCount() == 1) {
      reportIssue(context, visibilitySection, MESSAGE);
    }
    return super.visit(visibilitySection, context);
  }
}
