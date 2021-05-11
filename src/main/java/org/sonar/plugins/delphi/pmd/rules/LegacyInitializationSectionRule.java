package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;

public class LegacyInitializationSectionRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(DelphiAST ast, RuleContext data) {
    if (ast.isUnit()) {
      DelphiNode compoundStatement = ast.getFirstChildOfType(CompoundStatementNode.class);
      if (compoundStatement != null) {
        addViolation(data, compoundStatement.jjtGetFirstToken());
      }
    }
    return data;
  }
}
