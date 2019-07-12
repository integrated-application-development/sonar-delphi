package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Class for counting line characters. If too long, creates a violation.
 */
public class TooLongLineRule extends DelphiRule {

  private static final String MESSAGE = "Line too long (%s characters). Maximum character count "
      + "should be %s.";

  private int lastLineChecked;

  @Override
  public void init() {
    lastLineChecked = 0;
  }

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    // Retrieve and store the astTree from the first node
    int lineNumber = node.getLine();

    if (lineNumber > lastLineChecked) {
      String line = node.getASTTree().getFileSourceLine(lineNumber);

      int lineLength = getLineLengthWithoutComment(line);
      int lineLimit = getProperty(LIMIT);

      if (lineLength > lineLimit) {
        newViolation(ctx)
            .fileLocation(node.getLine(), 0, node.getLine(), lineLength - 1)
            .logicalLocation(node)
            .message(String.format(MESSAGE, lineLength, lineLimit))
            .save();
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
