package org.sonar.plugins.delphi.pmd.rules;

import java.util.Set;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.XPathRule;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;

public abstract class AbstractXPathRule extends XPathRule implements DelphiRule {
  private Set<Integer> suppressions;

  protected AbstractXPathRule() {
    defineBaseProperties();
  }

  @Override
  public void addViolation(Object data, Node node, String arg) {
    suppressions = ((DelphiNode) node).getASTTree().getSuppressions();
    super.addViolation(data, node, arg);
  }

  @Override
  public boolean isSuppressedLine(int line) {
    return suppressions.contains(line);
  }
}
