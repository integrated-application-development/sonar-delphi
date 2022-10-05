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
package org.sonar.plugins.delphi.pmd.rules;

import static java.util.regex.Pattern.compile;

import com.google.common.base.Splitter;
import java.util.regex.Pattern;
import net.sourceforge.pmd.RuleContext;
import org.apache.commons.lang3.StringUtils;
import org.sonar.plugins.delphi.antlr.ast.token.DelphiToken;
import org.sonar.plugins.delphi.pmd.FilePosition;

public class TrailingWhitespaceRule extends AbstractDelphiRule {
  private static final Pattern NEW_LINE_DELIMITER = compile("\r\n?|\n");

  @Override
  public void visitToken(DelphiToken token, RuleContext data) {
    if (!token.isWhitespace()) {
      return;
    }

    if (!NEW_LINE_DELIMITER.matcher(token.getImage()).find()) {
      return;
    }

    int line = token.getBeginLine();
    int column = token.getBeginColumn();

    String image = StringUtils.stripEnd(token.getImage(), " \t\f");
    var parts = Splitter.on(NEW_LINE_DELIMITER).split(image);
    for (String whitespace : parts) {
      if (!whitespace.isEmpty()) {
        newViolation(data)
            .atPosition(FilePosition.from(line, column, line, column + whitespace.length()))
            .save();
      }
      ++line;
      column = 0;
    }
  }
}
