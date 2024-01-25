/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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

import au.com.integradev.delphi.utils.format.DelphiFormatString;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArrayConstructorNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;

@Rule(key = "FormatArgumentCount")
public class FormatArgumentCountCheck extends AbstractFormatArgumentCheck {
  @Override
  protected void checkFormatStringViolation(
      DelphiFormatString formatString,
      ArrayConstructorNode arrayConstructor,
      DelphiCheckContext context) {
    int expectedArgs = formatString.getArguments().size();
    int actualArgs = arrayConstructor.getElements().size();
    int difference = expectedArgs - actualArgs;
    String plural = Math.abs(difference) == 1 ? "" : "s";
    if (difference > 0) {
      reportIssue(
          context,
          arrayConstructor,
          String.format(
              "Add %d more formatting argument%s to this 'Format' call.", difference, plural));
    } else if (difference < 0) {
      reportIssue(
          context,
          arrayConstructor,
          String.format(
              "Remove %d formatting argument%s from this 'Format' call.",
              Math.abs(difference), plural));
    }
  }
}
