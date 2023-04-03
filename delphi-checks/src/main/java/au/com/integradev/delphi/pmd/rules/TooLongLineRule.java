/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.pmd.rules;

import static au.com.integradev.delphi.pmd.DelphiPmdConstants.LIMIT;

import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import net.sourceforge.pmd.RuleContext;

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
      String line = node.getAst().getDelphiFile().getSourceCodeLine(lineNumber);
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
