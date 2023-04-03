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

import static au.com.integradev.delphi.pmd.DelphiPmdConstants.BUILTIN_XPATH;

import net.sourceforge.pmd.RuleContext;
import org.apache.commons.lang3.StringUtils;

public class XPathBuiltinRule extends AbstractXPathRule {
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