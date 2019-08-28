package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.ExceptItemNode;

public class CatchingGeneralExceptionRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(ExceptItemNode exceptItem, RuleContext data) {
    if (exceptItem.getExceptionType().hasImageEqualTo("Exception")) {
      addViolation(data, exceptItem);
    }
    return super.visit(exceptItem, data);
  }
}
