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

import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.DecimalLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.IntegerLiteralNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

@Rule(key = "DigitGrouping")
public class DigitGroupingCheck extends DelphiCheck {

  private static final Pattern VALID_DEC_UNDERSCORE_PATTERN = Pattern.compile("\\d{1,3}(_\\d{3})*");
  private static final Pattern VALID_DEC_FLOAT_UNDERSCORE_PATTERN =
      Pattern.compile(VALID_DEC_UNDERSCORE_PATTERN.pattern() + "\\..*");

  private static final Pattern VALID_HEX_UNDERSCORE_PATTERN =
      Pattern.compile(
          "(?i)\\$("
              + "([A-F0-9]{1,2}(_[A-F0-9]{2})*)"
              + "|"
              + "([A-F0-9]{1,4}(_[A-F0-9]{4})*)"
              + ")");

  private static final Pattern VALID_BIN_UNDERSCORE_PATTERN =
      Pattern.compile(
          "%("
              + "([01]{1,2}(_[01]{2})*)"
              + "|"
              + "([01]{1,3}(_[01]{3})*)"
              + "|"
              + "([01]{1,4}(_[01]{4})*)"
              + ")");

  public static final String MESSAGE = "Use standard digit groupings in this numeric literal.";

  @Override
  public DelphiCheckContext visit(IntegerLiteralNode literal, DelphiCheckContext context) {
    if (isCheckRelevant(literal.getToken()) && invalidIntegerLiteral(literal)) {
      reportIssue(context, literal, MESSAGE);
    }

    return super.visit(literal, context);
  }

  @Override
  public DelphiCheckContext visit(DecimalLiteralNode literal, DelphiCheckContext context) {
    if (isCheckRelevant(literal.getToken()) && invalidFloat(literal)) {
      reportIssue(context, literal, MESSAGE);
    }

    return super.visit(literal, context);
  }

  private static boolean isCheckRelevant(DelphiToken token) {
    return token.getImage().contains("_");
  }

  private static boolean invalidIntegerLiteral(IntegerLiteralNode integerLiteral) {
    switch (integerLiteral.getRadix()) {
      case 2:
        return invalidBin(integerLiteral);
      case 10:
        return invalidDecimal(integerLiteral);
      case 16:
        return invalidHex(integerLiteral);
      default:
        return false;
    }
  }

  private static boolean invalidDecimal(IntegerLiteralNode literalNode) {
    return notMatching(VALID_DEC_UNDERSCORE_PATTERN, literalNode.getToken());
  }

  private static boolean invalidFloat(DecimalLiteralNode literalNode) {
    return notMatching(VALID_DEC_FLOAT_UNDERSCORE_PATTERN, literalNode.getToken());
  }

  private static boolean invalidHex(IntegerLiteralNode literalNode) {
    return notMatching(VALID_HEX_UNDERSCORE_PATTERN, literalNode.getToken());
  }

  private static boolean invalidBin(IntegerLiteralNode literalNode) {
    return notMatching(VALID_BIN_UNDERSCORE_PATTERN, literalNode.getToken());
  }

  private static boolean notMatching(Pattern pattern, DelphiToken token) {
    return !pattern.matcher(token.getImage()).matches();
  }
}
