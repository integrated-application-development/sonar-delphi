package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.MethodHeadingNode;

public class NoFunctionReturnTypeRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(MethodHeadingNode method, RuleContext data) {
    if (method.isFunction() && method.getMethodReturnType() == null) {
      addViolation(data, method);
    }
    return super.visit(method, data);
  }
}
