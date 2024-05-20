/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package au.com.integradev.delphi.utils;

import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

public final class IndentationUtils {
  private IndentationUtils() {
    // Utility class
  }

  /**
   * Gets the leading whitespace of a source code line.
   *
   * @param node Any node starting on the indented line.
   * @return a string containing the leading whitespace.
   */
  public static String getLineIndentation(DelphiNode node) {
    return getLineIndentation(
        node.getAst().getDelphiFile().getSourceCodeFileLines().get(node.getBeginLine() - 1));
  }

  /**
   * Gets the leading whitespace of a string.
   *
   * @param line the string to search.
   * @return a string containing the leading whitespace.
   */
  private static String getLineIndentation(String line) {
    for (int i = 0; i < line.length(); i++) {
      if (line.charAt(i) != '\t' && line.charAt(i) != ' ') {
        return line.substring(0, i);
      }
    }

    return line;
  }
}
