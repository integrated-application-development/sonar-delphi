package au.com.integradev.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "NoGuidRule", repositoryKey = "delph")
@Rule(key = "InterfaceGuid")
public class InterfaceGuidCheck extends DelphiCheck {
  private static final String MESSAGE = "Add a GUID to this interface.";

  @Override
  public DelphiCheckContext visit(TypeDeclarationNode typeDeclaration, DelphiCheckContext context) {
    if (typeDeclaration.isInterface()) {
      InterfaceTypeNode interfaceType = (InterfaceTypeNode) typeDeclaration.getTypeNode();
      if (!interfaceType.isForwardDeclaration() && interfaceType.getGuid() == null) {
        reportIssue(context, typeDeclaration.getTypeNameNode(), MESSAGE);
      }
    }
    return super.visit(typeDeclaration, context);
  }
}
