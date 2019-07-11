package org.sonar.plugins.delphi.pmd.rules;

import java.util.HashSet;
import java.util.Set;
import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Class for counting line characters. If too long, creates a violation.
 */
public class TooLongLineRule extends DelphiRule {

  private int lineLimit;
  private final Set<Integer> checkedLines = new HashSet<>();
  private Tree astTree;
  private boolean firstNode;

  @Override
  protected void init() {
    super.init();
    lineLimit = getProperty(LIMIT);
    astTree = null;
    firstNode = true;
  }

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    // Retrieve and store the astTree from the first node
    if (firstNode) {
      astTree = node.getASTTree();
      firstNode = false;
    }

    int lineNumber = node.getLine();
    if (!checkedLines.contains(
        lineNumber)) {
      // Only check a line that has not been checked before
      checkedLines.add(lineNumber);
      String line = ((ASTTree) astTree).getFileSourceLine(lineNumber);

      int lineLength = getLineLengthWithoutComment(line);

      if (lineLength > lineLimit) {
        String sonarMessage =
            "Line too long (" + lineLength + " characters). Maximum character count should be "
                + lineLimit + ".";
        addViolation(ctx, node, sonarMessage);
      }

    }
  }

  private int getLineLengthWithoutComment(String line) {
    int commentIndex = line.indexOf("//");
    return (commentIndex == -1) ? line.length() : ++commentIndex;
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
