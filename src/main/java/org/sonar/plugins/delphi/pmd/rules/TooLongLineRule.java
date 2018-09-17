package org.sonar.plugins.delphi.pmd.rules;

import java.util.ArrayList;
import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Class for counting line characters. If too long, creates a violation.
 */
public class TooLongLineRule extends DelphiRule {

  private int limit;
  private ArrayList checkedLines = new ArrayList<Integer>();
  private Tree astTree;
  private boolean firstNode;

  @Override
  protected void init() {
    super.init();
    limit = getProperty(LIMIT);
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
      // Remove comment
      line = removeComment(line);

      if (line.length() > limit) {
        String sonarMessage =
            "Line too long (" + line.length() + " characters). Maximum character count should be "
                + limit + ".";
        addViolation(ctx, node, sonarMessage);
      }

    }
  }

  private String removeComment(String line) {
    return line.replaceAll("(\\s+)?(\\/\\/)(.+)", "");
  }

}
