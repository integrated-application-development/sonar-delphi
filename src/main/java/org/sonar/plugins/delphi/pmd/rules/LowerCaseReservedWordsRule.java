package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.core.DelphiKeywords;

public class LowerCaseReservedWordsRule extends DelphiRule {

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    Tree parent = node.getParent();
    if (parent.getType() == DelphiLexer.TkAssemblerInstructions) {
      return;
    }

    if (!DelphiKeywords.KEYWORDS.contains(node.getType())) {
      return;
    }

    String keywordName = node.getText();
    if (StringUtils.isAllLowerCase(keywordName)) {
      return;
    }

    addViolation(ctx, node);
  }
}
