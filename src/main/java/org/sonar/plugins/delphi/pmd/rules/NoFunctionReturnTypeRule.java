package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;

public class NoFunctionReturnTypeRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    if (method.isFunction() && method.getMethodHeading().getMethodReturnType() == null) {
      addViolation(data, method);
    }
    return super.visit(method, data);
  }
}
