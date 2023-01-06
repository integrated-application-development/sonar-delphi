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

import java.util.regex.Pattern;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.antlr.ast.node.TextLiteralNode;

public class StringLiteralRegexRule extends AbstractDelphiRule {
  private static final Logger LOG = Loggers.get(StringLiteralRegexRule.class);

  public static final PropertyDescriptor<String> REGEX =
      PropertyFactory.stringProperty("regex")
          .desc("The regular expression")
          .defaultValue("(?!)")
          .build();

  private static final PropertyDescriptor<String> MESSAGE =
      PropertyFactory.stringProperty("message").desc("The issue message").defaultValue("").build();

  private Pattern pattern;

  public StringLiteralRegexRule() {
    definePropertyDescriptor(REGEX);
    definePropertyDescriptor(MESSAGE);
  }

  @Override
  public void start(RuleContext ctx) {
    String regularExpression = getProperty(REGEX);

    try {
      pattern = Pattern.compile(regularExpression, Pattern.DOTALL);
    } catch (IllegalArgumentException e) {
      LOG.debug("Unable to compile regular expression: " + regularExpression, e);
    }
  }

  @Override
  public RuleContext visit(TextLiteralNode string, RuleContext data) {
    if (pattern != null && pattern.matcher(string.getImageWithoutQuotes()).matches()) {
      addViolationWithMessage(data, string, getProperty(MESSAGE));
    }

    return super.visit(string, data);
  }

  @Override
  public String dysfunctionReason() {
    start(null);
    return pattern == null ? ("Unable to compile regular expression: " + getProperty(REGEX)) : null;
  }
}
