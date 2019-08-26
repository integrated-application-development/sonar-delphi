package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodParametersNode;

public class EmptyBracketsRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(MethodParametersNode parameters, RuleContext data) {
    if (parameters.isEmpty()) {
      addViolation(data, parameters);
    }
    return super.visit(parameters, data);
  }

  @Override
  public RuleContext visit(ArgumentListNode arguments, RuleContext data) {
    if (arguments.isEmpty()) {
      addViolation(data, arguments);
    }
    return super.visit(arguments, data);
  }
}
