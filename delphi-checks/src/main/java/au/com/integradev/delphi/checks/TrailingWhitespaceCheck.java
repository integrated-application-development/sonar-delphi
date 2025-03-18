/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.checks;

import static java.util.regex.Pattern.compile;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "TrailingWhitespaceRule", repositoryKey = "delph")
@Rule(key = "TrailingWhitespace")
public class TrailingWhitespaceCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this trailing whitespace.";

  private static final Pattern NEW_LINE_DELIMITER = compile("\r\n?|\n");
  private static final Splitter NEW_LINE_SPLITTER = Splitter.on(NEW_LINE_DELIMITER);
  private static final CharMatcher WHITESPACE_MATCHER =
      CharMatcher.inRange((char) 0x00, (char) 0x20)
          .and(CharMatcher.isNot('\r'))
          .and(CharMatcher.isNot('\n'));

  @Override
  public void visitToken(DelphiToken token, DelphiCheckContext context) {
    visitWhitespace(token, context);
    visitComment(token, context);
  }

  private static void visitWhitespace(DelphiToken token, DelphiCheckContext context) {
    if (!token.isWhitespace()) {
      return;
    }

    if (!NEW_LINE_DELIMITER.matcher(token.getImage()).find()) {
      return;
    }

    String image = WHITESPACE_MATCHER.trimTrailingFrom(token.getImage());
    List<String> whitespaces = NEW_LINE_SPLITTER.splitToList(image);

    for (int i = 0; i < whitespaces.size(); ++i) {
      String whitespace = whitespaces.get(i);
      if (whitespace.isEmpty()) {
        continue;
      }

      int line = token.getBeginLine() + i;
      int column;

      if (i == 0) {
        column = token.getBeginColumn();
      } else {
        column = 0;
      }

      var whitespacePosition = FilePosition.from(line, column, line, column + whitespace.length());

      context
          .newIssue()
          .onFilePosition(whitespacePosition)
          .withMessage(MESSAGE)
          .withQuickFixes(
              QuickFix.newFix("Remove trailing whitespace")
                  .withEdit(QuickFixEdit.delete(whitespacePosition)))
          .report();
    }
  }

  private static void visitComment(DelphiToken token, DelphiCheckContext context) {
    if (!token.isComment()) {
      return;
    }

    var commentLines = NEW_LINE_SPLITTER.splitToList(token.getImage());

    for (int i = 0; i < commentLines.size(); ++i) {
      String commentLine = commentLines.get(i);
      String trimmedCommentLine = WHITESPACE_MATCHER.trimTrailingFrom(commentLine);
      int whitespaceLength = commentLine.length() - trimmedCommentLine.length();

      if (whitespaceLength > 0) {
        int line = token.getBeginLine() + i;
        int column;

        if (i == 0) {
          column = token.getBeginColumn() + trimmedCommentLine.length();
        } else {
          column = trimmedCommentLine.length();
        }

        var whitespacePosition = FilePosition.from(line, column, line, column + whitespaceLength);

        context
            .newIssue()
            .onFilePosition(whitespacePosition)
            .withMessage(MESSAGE)
            .withQuickFixes(
                QuickFix.newFix("Remove trailing whitespace")
                    .withEdit(QuickFixEdit.delete(whitespacePosition)))
            .report();
      }
    }
  }
}
