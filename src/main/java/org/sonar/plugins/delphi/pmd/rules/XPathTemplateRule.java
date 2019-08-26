package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

public class XPathTemplateRule extends AbstractXPathRule {

  private static final PropertyDescriptor<String> MESSAGE =
      PropertyFactory.stringProperty("message").desc("The issue message").defaultValue("").build();

  public XPathTemplateRule() {
    definePropertyDescriptor(MESSAGE);
  }
}
