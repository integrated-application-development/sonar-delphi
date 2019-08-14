package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;

public class XPathTemplateRule extends XPathRule {

  private static final PropertyDescriptor<String> XPATH =
      PropertyFactory.stringProperty(DelphiPmdConstants.TEMPLATE_XPATH_EXPRESSION_PARAM)
          .desc("The xpath expression")
          .defaultValue("")
          .build();

  private static final PropertyDescriptor<String> MESSAGE =
      PropertyFactory.stringProperty("message").desc("The issue message").defaultValue("").build();

  public XPathTemplateRule() {
    definePropertyDescriptor(XPATH);
    definePropertyDescriptor(MESSAGE);
  }

  @Override
  protected String getXPathExpression() {
    return getProperty(XPATH);
  }

  @Override
  protected String getViolationMessage() {
    return getProperty(MESSAGE);
  }
}
