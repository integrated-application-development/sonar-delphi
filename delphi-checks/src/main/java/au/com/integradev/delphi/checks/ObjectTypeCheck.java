package au.com.integradev.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "ObjectTypeRule", repositoryKey = "delph")
@Rule(key = "ObjectType")
public class ObjectTypeCheck extends DelphiCheck {
  private static final String MESSAGE = "Change this 'object' type into a class.";

  @Override
  public DelphiCheckContext visit(TypeDeclarationNode typeDeclaration, DelphiCheckContext context) {
    if (typeDeclaration.isObject()) {
      reportIssue(context, typeDeclaration.getTypeNameNode(), MESSAGE);
    }
    return super.visit(typeDeclaration, context);
  }
}
