/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
package org.sonar.plugins.communitydelphi.pmd.xml;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.pmd.DelphiPmdConstants;

class DelphiPropertyTest {

  @Test
  void testConstructors() {
    DelphiRuleProperty property = new DelphiRuleProperty("name");
    assertThat(property.getName()).isEqualTo("name");
    assertThat(property.getValue()).isNull();

    property = new DelphiRuleProperty("name", "value");
    assertThat(property.getName()).isEqualTo("name");
    assertThat(property.getValue()).isEqualTo("value");
  }

  @Test
  void testIsCdataValue() {
    DelphiRuleProperty property = new DelphiRuleProperty("name", "value");
    assertThat(property.isCdataValue()).isFalse();

    property = new DelphiRuleProperty(DelphiPmdConstants.TEMPLATE_XPATH_EXPRESSION_PARAM);
    assertThat(property.isCdataValue()).isTrue();

    property = new DelphiRuleProperty(DelphiPmdConstants.BUILTIN_XPATH_EXPRESSION_PARAM);
    assertThat(property.isCdataValue()).isTrue();
  }
}
