package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.LIMIT;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;

/**
 * This rule adds violations when there are too many defined sub-procedures in a top-level method,
 * regardless of sub-procedure nesting level
 */
public class TooManySubProceduresRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    int count = countSubProcedures(method);
    int limit = getProperty(LIMIT);

    if (count > limit) {
      addViolationWithMessage(
          data,
          method.getMethodNameNode(),
          "Code should not contain too many sub-procedures."
              + " Method has {0} sub-procedures (Limit is {1})",
          new Object[] {count, limit});
    }

    return data;
  }

  private int countSubProcedures(MethodImplementationNode method) {
    return method.findDescendantsOfType(MethodImplementationNode.class).size();
  }
}
