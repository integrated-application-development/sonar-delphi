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

import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.TextLiteralNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.IllegalRuleParameterError;
import org.sonar.plugins.communitydelphi.api.check.RuleTemplate;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@RuleTemplate
@DeprecatedRuleKey(ruleKey = "StringLiteralRegexRule", repositoryKey = "delph")
@Rule(key = "StringLiteralRegularExpression")
public class StringLiteralRegularExpressionCheck extends DelphiCheck {
  private static final String DEFAULT_REGULAR_EXPRESSION = "(?!)";
  private static final String DEFAULT_MESSAGE = "The regular expression matches this string.";

  private Pattern pattern;

  @RuleProperty(
      key = "regex",
      description = "The regular expression",
      defaultValue = DEFAULT_REGULAR_EXPRESSION)
  public String regex = DEFAULT_REGULAR_EXPRESSION;

  @RuleProperty(key = "message", description = "The issue message", defaultValue = DEFAULT_MESSAGE)
  public String message = DEFAULT_MESSAGE;

  @Override
  public void start(DelphiCheckContext context) {
    if (pattern == null) {
      try {
        pattern = Pattern.compile(regex, Pattern.DOTALL);
      } catch (IllegalArgumentException e) {
        throw new IllegalRuleParameterError("Unable to compile regular expression: " + regex, e);
      }
    }
  }

  @Override
  public DelphiCheckContext visit(TextLiteralNode string, DelphiCheckContext context) {
    if (pattern != null && pattern.matcher(string.getValue()).matches()) {
      reportIssue(context, string, message);
    }
    return super.visit(string, context);
  }
}
