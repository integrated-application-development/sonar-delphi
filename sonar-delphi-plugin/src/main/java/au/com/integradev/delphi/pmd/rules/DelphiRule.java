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

import static au.com.integradev.delphi.pmd.DelphiPmdConstants.BASE_EFFORT;
import static au.com.integradev.delphi.pmd.DelphiPmdConstants.LIMIT;
import static au.com.integradev.delphi.pmd.DelphiPmdConstants.SCOPE;
import static au.com.integradev.delphi.pmd.DelphiPmdConstants.TEMPLATE;
import static au.com.integradev.delphi.pmd.DelphiPmdConstants.TYPE;

import au.com.integradev.delphi.antlr.ast.token.DelphiToken;
import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import au.com.integradev.delphi.pmd.FilePosition;
import au.com.integradev.delphi.pmd.violation.DelphiRuleViolationBuilder;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.rule.ImmutableLanguage;

public interface DelphiRule extends Rule, DelphiParserVisitor<RuleContext>, ImmutableLanguage {

  /**
   * Returns true if the line should have issues suppressed
   *
   * @param line the line to check
   * @return true if the line should have issues suppressed
   * @see au.com.integradev.delphi.pmd.DelphiPmdConstants#SUPPRESSION_TAG
   */
  boolean isSuppressedLine(int line);

  default void defineBaseProperties() {
    definePropertyDescriptor(BASE_EFFORT);
    definePropertyDescriptor(LIMIT);
    definePropertyDescriptor(SCOPE);
    definePropertyDescriptor(TEMPLATE);
    definePropertyDescriptor(TYPE);
  }

  /**
   * Adds violation to pmd report for a token
   *
   * @param data Visitor data (RuleContext)
   * @param token Violation token
   */
  default void addViolation(Object data, DelphiToken token) {
    newViolation(data).atPosition(FilePosition.from(token)).save();
  }

  /**
   * Adds violation to pmd report for a token (with override message)
   *
   * @param data Visitor data (RuleContext)
   * @param token Violation token
   * @param msg Violation message
   */
  default void addViolation(Object data, DelphiToken token, String msg) {
    newViolation(data).atPosition(FilePosition.from(token)).message(msg).save();
  }

  default DelphiRuleViolationBuilder newViolation(Object data) {
    return DelphiRuleViolationBuilder.newViolation(this, (RuleContext) data);
  }
}
