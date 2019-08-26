package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.apache.commons.lang3.StringUtils;

public class XPathBuiltinRule extends AbstractXPathRule {
  public static final PropertyDescriptor<String> BUILTIN_XPATH =
      PropertyFactory.stringProperty("builtinXPath")
          .desc("The xpath expression")
          .defaultValue("")
          .build();

  public XPathBuiltinRule() {
    definePropertyDescriptor(BUILTIN_XPATH);
  }

  @Override
  public void start(RuleContext ctx) {
    this.setXPath(getProperty(BUILTIN_XPATH));
  }

  @Override
  public String dysfunctionReason() {
    return StringUtils.isBlank(getProperty(BUILTIN_XPATH)) ? "Missing xPath expression" : null;
  }
}
