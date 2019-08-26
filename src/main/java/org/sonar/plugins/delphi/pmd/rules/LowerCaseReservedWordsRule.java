package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.delphi.antlr.ast.node.AsmStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.core.DelphiKeywords;

public class LowerCaseReservedWordsRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(DelphiNode node, RuleContext data) {
    if (isViolationNode(node)) {
      addViolation(data, node.getToken());
    }
    return super.visit(node, data);
  }

  @Override
  public RuleContext visit(AsmStatementNode node, RuleContext data) {
    // Do not look inside of asm blocks
    return data;
  }

  private boolean isViolationNode(DelphiNode node) {
    if (!DelphiKeywords.KEYWORDS.contains(node.jjtGetId())) {
      return false;
    }

    return !StringUtils.isAllLowerCase(node.getToken().getImage());
  }
}
