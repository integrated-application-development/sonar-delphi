package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/** Class for counting line characters. If too long, creates a violation. */
public class TooLongLineRule extends DelphiRule {

  private static final String MESSAGE =
      "Line too long (%s characters). Maximum length is %s " + "characters.";

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
      lastLineChecked = lineNumber;
      String line = node.getASTTree().getFileSourceLine(lineNumber);
      int lineLength = getLineLength(line);
      int lineLimit = getProperty(LIMIT);

      if (lineLength > lineLimit) {
        newViolation(ctx)
            .fileLocation(lineNumber, 0, lineNumber, lineLength)
            .logicalLocation(node)
            .message(String.format(MESSAGE, lineLength, lineLimit))
            .save();
      }
    }
  }

  private int getLineLength(String line) {
    int length = line.length();

    // For the purposes of line length, we don't care about whitespace at the end of the line
    for (; length > 0; length--) {
      if (!Character.isWhitespace(line.charAt(length - 1))) {
        break;
      }
    }

    return length;
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
