package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.LIMIT;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.BlockDeclarationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarSectionNode;

public class TooManyVariablesRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    int count = countVariableDeclarations(method);
    int limit = getProperty(LIMIT);
    if (count > limit) {
      addViolationWithMessage(
          data,
          method.getMethodNameNode(),
          "Too many variables: {0} (max {1})",
          new Object[] {count, limit});
    }
    return super.visit(method, data);
  }

  private static int countVariableDeclarations(MethodImplementationNode method) {
    int count = 0;
    BlockDeclarationSectionNode declSection = method.getDeclarationSection();
    if (declSection != null) {
      List<VarSectionNode> varSections = declSection.findChildrenOfType(VarSectionNode.class);
      for (VarSectionNode varSection : varSections) {
        count += varSection.getDeclarations().size();
      }
    }
    return count;
  }
}
