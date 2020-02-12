package org.sonar.plugins.delphi.pmd;

import net.sourceforge.pmd.lang.AbstractLanguageVersionHandler;
import net.sourceforge.pmd.lang.Parser;
import net.sourceforge.pmd.lang.ParserOptions;
import net.sourceforge.pmd.lang.XPathHandler;
import net.sourceforge.pmd.lang.ast.xpath.DefaultASTXPathHandler;
import net.sourceforge.pmd.lang.rule.RuleViolationFactory;
import org.sonar.plugins.delphi.pmd.violation.DelphiRuleViolationFactory;
import org.sonar.plugins.delphi.pmd.xpath.TypeInheritsFromFunction;
import org.sonar.plugins.delphi.pmd.xpath.TypeIsExactlyFunction;
import org.sonar.plugins.delphi.pmd.xpath.TypeIsFunction;

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
