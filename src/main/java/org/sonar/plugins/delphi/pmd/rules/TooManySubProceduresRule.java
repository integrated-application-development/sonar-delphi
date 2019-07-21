package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
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
    if (node.getType() != DelphiLexer.IMPLEMENTATION) {
      return;
    }

    List children = node.getChildren();
    // Some implementation nodes may not have any code yet, hence no children
    if (children == null) {
      return;
    }

    int subProcedureCounter = 0;

    // subProcedureDepth tracks whether we are in a procedure, sub procedure,
    // or sub-sub procedures.
    // -1 means not in a procedure
    // 0 means in an out procedure
    // 1 means in a sub procedure
    // n means in a nth level sub procedure
    // we treat any nth level sub procedure the same as any sub procedures
    int subProcedureDepth = -1;

    for (Object child : children) {
      String value = child.toString();

      if (value.equals("begin")) {
        --subProcedureDepth;
      }

      if (!value.equals("procedure") && !value.equals("function")) {
        continue;
      }

      ++subProcedureDepth;

      if (subProcedureDepth > 0) {
        ++subProcedureCounter;
      }

      if (subProcedureCounter > getProperty(LIMIT)) {
        addViolation(ctx, node,
            "Code should not contain too many sub-procedures, " +
                "limit of " + getProperty(LIMIT) + " exceeded.");
        // Avoid adding multiple violations of same type
        return;
      }
    }
  }
}
