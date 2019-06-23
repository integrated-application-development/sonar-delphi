package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;


public class DuplicatesRule extends DelphiRule {

  /**
   * This rule adds violations when the Duplicates method, (foo.Duplicates := dupError) is called on
   * a list, but the preceding line did not first sort the list (using foo.Sorted := True)
   *
   * @param node the current node
   * @param ctx the ruleContext to store the violations
   */

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    List children = node.getChildren();
    if (children != null) {

      for (int i = 0; i < children.size() - 4; i++) {
        if (children.get(i).toString().equals(".") &&
            children.get(i + 1).toString().equals("Duplicates")) {
          // Found a call to duplicates function
          try {
            if (!children.get(i - 1).toString().equals(children.get(i - 7).toString())
                // ensure same list
                // Ensure list is being sorted
                || !children.get(i - 6).toString().equals(".")
                || !children.get(i - 5).toString().equals("Sorted")
                || !children.get(i - 4).toString().equals(":=")
                || !children.get(i - 3).toString().equals("True")) {
              addViolation(ctx, (DelphiPMDNode) children.get(i - 1));
            }
          } catch (IndexOutOfBoundsException e) {
            // If an index was raised, then there could not have been a sort performed on the
            // previous line, so raise a violation
            addViolation(ctx, (DelphiPMDNode) children.get(i - 1));
          }
        }
      }
    }
  }
}
