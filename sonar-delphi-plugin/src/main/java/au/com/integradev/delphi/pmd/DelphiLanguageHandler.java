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
package au.com.integradev.delphi.pmd;

import au.com.integradev.delphi.pmd.violation.DelphiRuleViolationFactory;
import au.com.integradev.delphi.pmd.xpath.TypeInheritsFromFunction;
import au.com.integradev.delphi.pmd.xpath.TypeIsExactlyFunction;
import au.com.integradev.delphi.pmd.xpath.TypeIsFunction;
import net.sourceforge.pmd.lang.AbstractLanguageVersionHandler;
import net.sourceforge.pmd.lang.Parser;
import net.sourceforge.pmd.lang.ParserOptions;
import net.sourceforge.pmd.lang.XPathHandler;
import net.sourceforge.pmd.lang.ast.xpath.DefaultASTXPathHandler;
import net.sourceforge.pmd.lang.rule.RuleViolationFactory;

public class DelphiLanguageHandler extends AbstractLanguageVersionHandler {
  private final DelphiRuleViolationFactory violationFactory = new DelphiRuleViolationFactory();

  @Override
  public RuleViolationFactory getRuleViolationFactory() {
    return violationFactory;
  }

  /**
   * Returns DelphiLanguage XPath handler.
   *
   * <p>XPathHandlers are deprecated and will be removed when they're replaced in PMD 7.0, but until
   * then this is the only way of using PMD's XPath implementation.
   *
   * @return XPath Handler
   */
  @Override
  @SuppressWarnings("deprecation")
  public XPathHandler getXPathHandler() {
    return new DefaultASTXPathHandler() {
      @Override
      public void initialize() {
        TypeIsFunction.registerSelfInSimpleContext();
        TypeIsExactlyFunction.registerSelfInSimpleContext();
        TypeInheritsFromFunction.registerSelfInSimpleContext();
      }
    };
  }

  @Override
  public Parser getParser(ParserOptions options) {
    throw new UnsupportedOperationException("Out of scope for sonar plugin");
  }
}
