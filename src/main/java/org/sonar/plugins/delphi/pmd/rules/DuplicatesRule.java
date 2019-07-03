package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;


public class DuplicatesRule extends DelphiRule {
  private static final String[] DUPLICATES_LINE = {".", "duplicates"};
  private static final String[] SORTED_LINE = {".", "sorted", ":=", "true"};
  private static final int MINIMUM_NODES = 4;
  private static final int PREVIOUS_LINE_OFFSET = 7;
  private static final int NEXT_LINE_OFFSET = 5;

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
    if (children == null) {
      return;
    }

    for (int i = 0; i < children.size() - MINIMUM_NODES; i++) {
      if (isDuplicatesLine(children, i)) {
        // Found a call to duplicates function
        if (sortedOnPreviousLine(children, i) || sortedOnNextLine(children, i)) {
          return;
        }

        addViolation(ctx, (DelphiPMDNode) children.get(i - 1));
      }
    }
  }

  private boolean isDuplicatesLine(List children, int childIndex) {
    for (int i = 0; i < DUPLICATES_LINE.length; ++i) {
      String child = children.get(childIndex + i).toString();
      if (!child.equalsIgnoreCase(DUPLICATES_LINE[i])) {
        return false;
      }
    }

    return true;
  }

  private boolean sortedOnPreviousLine(List children, int childIndex) {
    if (childIndex - PREVIOUS_LINE_OFFSET < 0) {
      return false;
    }

    String currentLineIdentifier = children.get(childIndex - 1).toString();
    String previousLineIdentifier = children.get(childIndex - PREVIOUS_LINE_OFFSET).toString();

    if (!currentLineIdentifier.equalsIgnoreCase(previousLineIdentifier)) {
      return false;
    }

    for (int i = 0; i < SORTED_LINE.length; ++i) {
      String child = children.get(childIndex - PREVIOUS_LINE_OFFSET + i).toString();
      if (!child.equalsIgnoreCase(SORTED_LINE[i])) {
        return false;
      }
    }

    return true;
  }

  private boolean sortedOnNextLine(List children, int childIndex) {
    if (childIndex + NEXT_LINE_OFFSET + SORTED_LINE.length >= children.size()) {
      return false;
    }

    String currentLineIdentifier = children.get(childIndex - 1).toString();
    String nextLineIdentifier = children.get(childIndex + NEXT_LINE_OFFSET).toString();

    if (!currentLineIdentifier.equalsIgnoreCase(nextLineIdentifier)) {
      return false;
    }

    for (int i = 0; i < SORTED_LINE.length; ++i) {
      String child = children.get(childIndex + NEXT_LINE_OFFSET + i).toString();
      if (!child.equalsIgnoreCase(SORTED_LINE[i])) {
        return false;
      }
    }

    return true;
  }
}

