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
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
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

    int line = token.getBeginLine();
    int column = token.getBeginColumn();

    String image = WHITESPACE_MATCHER.trimTrailingFrom(token.getImage());
    var parts = NEW_LINE_SPLITTER.split(image);
    for (String whitespace : parts) {
      if (!whitespace.isEmpty()) {
        context
            .newIssue()
            .onFilePosition(FilePosition.from(line, column, line, column + whitespace.length()))
            .withMessage(MESSAGE)
            .report();
      }
      ++line;
      column = 0;
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
        int column = trimmedCommentLine.length();

        context
            .newIssue()
            .onFilePosition(FilePosition.from(line, column, line, column + whitespaceLength))
            .withMessage(MESSAGE)
            .report();
      }
    }
  }
}
