package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.LIMIT;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.pmd.FilePosition;

/** Class for counting line characters. If too long, creates a violation. */
public class TooLongLineRule extends AbstractDelphiRule {
  private int lastLineChecked;

  @Override
  public void start(RuleContext ctx) {
    lastLineChecked = 0;
  }

  @Override
  public RuleContext visit(DelphiNode node, RuleContext data) {
    // Retrieve and store the astTree from the first node
    int lineNumber = node.getBeginLine();

    if (lineNumber > lastLineChecked) {
      lastLineChecked = lineNumber;
      String line = node.getASTTree().getDelphiFile().getSourceCodeLine(lineNumber);
      int lineLength = getLineLength(line);
      int lineLimit = getProperty(LIMIT);

      if (lineLength > lineLimit) {
        newViolation(data)
            .atPosition(FilePosition.from(lineNumber, 0, lineNumber, lineLength))
            .atLocation(node)
            .message(
                String.format(
                    "Line too long (%s characters). Maximum length is %s characters.",
                    lineLength, lineLimit))
            .save();
      }
    }

    return super.visit(node, data);
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
}
