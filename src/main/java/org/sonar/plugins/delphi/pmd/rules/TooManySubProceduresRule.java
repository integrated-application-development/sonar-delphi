package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class TooManySubProceduresRule extends DelphiRule {

  /**
   * This rule adds violations when there are too many defined sub procedures in a defined
   * implementation block, as defined by a user defined LIMIT value.
   *
   * Subprocedures can be nested, so this rule attempts to account for that by searching at multiple
   * depths in the tree.
   *
   * @param node the current node
   * @param ctx the ruleContext to store the violations
   */
  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {

    Integer threshold = getProperty(LIMIT);

    if (node.getType() == DelphiLexer.IMPLEMENTATION) {
      List children = node.getChildren();
      int subProcedureCounter = 0;

      // subProcedureDepth tracks whether we are in a procedure, sub procedure, or sub-sub procedures.
      // -1 means not in a procedure
      // 0 means in an out procedure
      // 1 means in a sub procedure
      // n means in a nth level sub procedure
      // we treat any nth level sub procedure the same as any sub procedures
      int subProcedureDepth = -1;

      if (children
          != null) { // Some implementation nodes may not have any code yet, hence no children
        for (int i = 0; i < children.size(); i++) {

          if ((children.get(i).toString().equals("procedure")) || (children.get(i).toString()
              .equals("function"))) {
            subProcedureDepth += 1;
            if (subProcedureDepth > 0) {
              subProcedureCounter += 1;
            }
            if (subProcedureCounter > threshold) {
              addViolation(ctx, node,
                  "Code should not contain too many sub procedures or functions, " +
                      "limit of " + getProperty(LIMIT) + " exceeded.");
              break; // Avoid adding multiple violations of same type
            }
          } else if (children.get(i).toString().equals("begin")) {
            subProcedureDepth -= 1;
          }
        }
      }
    }
  }
}
