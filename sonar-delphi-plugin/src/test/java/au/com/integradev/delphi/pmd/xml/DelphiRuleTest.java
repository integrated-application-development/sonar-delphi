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
package au.com.integradev.delphi.pmd.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DelphiRuleTest {

  private DelphiRule rule;

  @BeforeEach
  void init() {
    rule = new DelphiRule("class", 1);
  }

  @Test
  void testName() {
    assertThat(rule.getName()).isNull();
    rule.setName("test");
    assertThat(rule.getName()).isEqualTo("test");
  }

  @Test
  void testClazz() {
    assertThat(rule.getClazz()).isEqualTo("class");
  }

  @Test
  void testProperties() {
    List<DelphiRuleProperty> properties = new ArrayList<>();
    properties.add(new DelphiRuleProperty("a", "b"));
    rule.setProperties(properties);

    assertThat(rule.getProperties()).isEqualTo(properties).hasSize(1);

    rule.addProperty(new DelphiRuleProperty("c", "d"));
    assertThat(rule.getProperties()).hasSize(2);

    rule.removeProperty("c");
    assertThat(rule.getProperties()).hasSize(1);

    assertThat(rule.getProperties().get(0).getName()).isEqualTo("a");
  }

  @Test
  void testPriority() {
    assertThat(rule.getPriority()).isEqualTo(1);
    assertThat(new DelphiRule("class").getPriority()).isNull();
  }

  @Test
  void testMessage() {
    rule.setMessage("my message");
    assertThat(rule.getMessage()).isEqualTo("my message");
  }

  @Test
  void testDescription() {
    assertThat(rule.getHtmlDescription()).isEmpty();
    rule.setDescription("abc");
    assertThat(rule.getDescription()).isEqualTo("abc");
    assertThat(rule.getHtmlDescription()).isEqualTo("<p>abc</p>");
    rule.setExample("123");
    assertThat(rule.getExample()).isEqualTo("123");
    assertThat(rule.getHtmlDescription()).isEqualTo("<p>abc</p><pre>123</pre>");
  }
}
