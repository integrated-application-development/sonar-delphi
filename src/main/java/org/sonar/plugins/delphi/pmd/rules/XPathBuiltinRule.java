package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;

public class XPathBuiltinRule extends XPathRule {
  public static final PropertyDescriptor<String> BUILTIN_XPATH =
      PropertyFactory.stringProperty(DelphiPmdConstants.BUILTIN_XPATH_EXPRESSION_PARAM)
          .desc("The xpath expression")
          .defaultValue("")
          .build();

  public XPathBuiltinRule() {
    definePropertyDescriptor(BUILTIN_XPATH);
  }

  @Override
  protected String getXPathExpression() {
    return getProperty(BUILTIN_XPATH);
  }

  @Override
  protected String getViolationMessage() {
    return getMessage();
  }
}
