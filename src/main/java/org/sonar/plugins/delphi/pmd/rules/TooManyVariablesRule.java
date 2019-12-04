package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.LIMIT;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.MethodBodyNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarSectionNode;

public class TooManyVariablesRule extends AbstractDelphiRule {
  private static final String VIOLATION_MESSAGE = "Too many variables: %d (max %d)";

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    int count = countVariableDeclarations(method);
    int limit = getProperty(LIMIT);
    if (count > limit) {
      addViolationWithMessage(
          data, method.getMethodNameNode(), String.format(VIOLATION_MESSAGE, count, limit));
    }
    return super.visit(method, data);
  }

  private static int countVariableDeclarations(MethodImplementationNode method) {
    int count = 0;
    MethodBodyNode body = method.getMethodBody();
    if (body.hasDeclarationSection()) {
      List<VarSectionNode> varSections =
          body.getDeclarationSection().findChildrenOfType(VarSectionNode.class);
      for (VarSectionNode varSection : varSections) {
        count += varSection.getDeclarations().size();
      }
    }
    return count;
  }
}
