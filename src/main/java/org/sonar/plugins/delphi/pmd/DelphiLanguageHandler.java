package org.sonar.plugins.delphi.pmd;

import net.sourceforge.pmd.lang.AbstractLanguageVersionHandler;
import net.sourceforge.pmd.lang.Parser;
import net.sourceforge.pmd.lang.ParserOptions;
import net.sourceforge.pmd.lang.XPathHandler;
import net.sourceforge.pmd.lang.ast.xpath.DefaultASTXPathHandler;
import net.sourceforge.pmd.lang.rule.RuleViolationFactory;
import org.sonar.plugins.delphi.pmd.violation.DelphiRuleViolationFactory;

public class DelphiLanguageHandler extends AbstractLanguageVersionHandler {
  private final DelphiRuleViolationFactory violationFactory = new DelphiRuleViolationFactory();

  @Override
  public RuleViolationFactory getRuleViolationFactory() {
    return violationFactory;
  }

  /**
   * Returns DelphiLanguage XPath handler. This function is deprecated and will be removed when it's
   * replaced in PMD 7.0, but until then this is the only way of using PMD's XPath implementation.
   *
   * @return XPath Handler
   */
  @Override
  @SuppressWarnings("deprecation")
  public XPathHandler getXPathHandler() {
    return new DefaultASTXPathHandler();
  }

  @Override
  public Parser getParser(ParserOptions options) {
    throw new UnsupportedOperationException("Out of scope for sonar plugin");
  }
}
