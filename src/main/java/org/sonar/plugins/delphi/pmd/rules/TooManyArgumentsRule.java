package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.LIMIT;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;

public class TooManyArgumentsRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    int count = method.getParameters().size();
    int limit = getProperty(LIMIT);
    if (count > limit) {
      addViolationWithMessage(
          data,
          method.getMethodNameNode(),
          "Too many arguments: {0} (max {1})",
          new Object[] {count, limit});
    }
    return super.visit(method, data);
  }
}
