package au.com.integradev.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.TypeSectionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "EmptyTypeSectionRule", repositoryKey = "delph")
@Rule(key = "EmptyTypeSection")
public class EmptyTypeSectionCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this empty type section.";

  @Override
  public DelphiCheckContext visit(TypeSectionNode typeSection, DelphiCheckContext context) {
    if (typeSection.getDeclarations().isEmpty()) {
      reportIssue(context, typeSection, MESSAGE);
    }
    return super.visit(typeSection, context);
  }
}
