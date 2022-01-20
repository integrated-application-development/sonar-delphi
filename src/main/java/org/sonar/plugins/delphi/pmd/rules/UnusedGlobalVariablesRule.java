package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.VariableUtils.isGeneratedFormVariable;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.delphi.symbol.scope.FileScope;

public class UnusedGlobalVariablesRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(VarDeclarationNode varDeclaration, RuleContext data) {
    if (varDeclaration.getScope() instanceof FileScope
        && !isGeneratedFormVariable(varDeclaration)) {
      varDeclaration.getNameDeclarationList().getDeclarations().stream()
          .filter(node -> node.getUsages().isEmpty())
          .forEach(node -> addViolation(data, node));
    }
    return data;
  }
}
