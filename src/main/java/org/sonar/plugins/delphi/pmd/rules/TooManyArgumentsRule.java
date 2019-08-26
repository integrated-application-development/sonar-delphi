package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.LIMIT;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;

public class TooManyArgumentsRule extends AbstractDelphiRule {

  private static final String VIOLATION_MESSAGE = "Too many arguments: %d (max %d)";

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    int count = method.getParameters().size();
    int limit = getProperty(LIMIT);
    if (count > limit) {
      addViolationWithMessage(
          data,
          method.getMethodHeading().getMethodName(),
          String.format(VIOLATION_MESSAGE, count, limit));
    }
    return super.visit(method, data);
  }
}
