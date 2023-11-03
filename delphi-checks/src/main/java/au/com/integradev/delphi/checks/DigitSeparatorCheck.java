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
package au.com.integradev.delphi.checks;

import org.apache.commons.lang3.StringUtils;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.DecimalLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.IntegerLiteralNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;

@Rule(key = "DigitSeparator")
public class DigitSeparatorCheck extends DelphiCheck {

  public static final String MESSAGE = "Separate this long numeric literal with underscores.";

  private static final int DEFAULT_MAX_DIGITS_WITHOUT_UNDERSCORES = 4;

  @RuleProperty(
      key = "maxDigitsWithoutUnderscores",
      description = "Maximum number of digits allowed without underscores.",
      defaultValue = DEFAULT_MAX_DIGITS_WITHOUT_UNDERSCORES + "")
  public int maxDigitsWithoutUnderscores = DEFAULT_MAX_DIGITS_WITHOUT_UNDERSCORES;

  @Override
  public DelphiCheckContext visit(IntegerLiteralNode literal, DelphiCheckContext context) {
    if (isIntMissingUnderscores(literal)) {
      reportIssue(context, literal, MESSAGE);
    }

    return super.visit(literal, context);
  }

  private boolean isIntMissingUnderscores(IntegerLiteralNode literal) {
    return literal.getDigits().length() > maxDigitsWithoutUnderscores
        && !StringUtils.contains(literal.getImage(), '_');
  }

  @Override
  public DelphiCheckContext visit(DecimalLiteralNode literal, DelphiCheckContext context) {
    if (isIntegerPartMissingUnderscores(literal.getImage())) {
      reportIssue(context, literal, MESSAGE);
    }

    return super.visit(literal, context);
  }

  private boolean isIntegerPartMissingUnderscores(String image) {
    String[] split = StringUtils.split(image, '.');
    return split.length >= 2
        && split[0].length() > maxDigitsWithoutUnderscores
        && !StringUtils.contains(split[0], '_');
  }
}
