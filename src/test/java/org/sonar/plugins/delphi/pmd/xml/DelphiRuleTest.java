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
package org.sonar.plugins.delphi.pmd.xml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.List;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DelphiRuleTest {

  private DelphiRule rule;

  @Rule public ExpectedException exceptionCatcher = ExpectedException.none();

  @Before
  public void init() {
    rule = new DelphiRule("class", 1);
  }

  @Test
  public void testName() {
    assertThat(rule.getName(), is(nullValue()));
    rule.setName("test");
    assertThat(rule.getName(), is("test"));
  }

  @Test
  public void testClazz() {
    assertThat(rule.getClazz(), is("class"));
  }

  @Test
  public void testProperties() {
    List<DelphiRuleProperty> properties = new ArrayList<>();
    properties.add(new DelphiRuleProperty("a", "b"));
    rule.setProperties(properties);

    assertThat(rule.getProperties(), is(properties));
    assertThat(rule.getProperties(), IsCollectionWithSize.hasSize(1));

    rule.addProperty(new DelphiRuleProperty("c", "d"));
    assertThat(rule.getProperties(), IsCollectionWithSize.hasSize(2));

    rule.removeProperty("c");
    assertThat(rule.getProperties(), IsCollectionWithSize.hasSize(1));

    assertThat(rule.getProperties().get(0).getName(), is("a"));
  }

  @Test
  public void testPriority() {
    assertThat(rule.getPriority(), is(1));
    assertThat(new DelphiRule("class").getPriority(), is(nullValue()));
  }

  @Test
  public void testMessage() {
    rule.setMessage("my message");
    assertThat(rule.getMessage(), is("my message"));
  }

  @Test
  public void testDescription() {
    assertThat(rule.getHtmlDescription(), isEmptyString());
    rule.setDescription("abc");
    assertThat(rule.getDescription(), is("abc"));
    assertThat(rule.getHtmlDescription(), is("<p>abc</p>"));
    rule.setExample("123");
    assertThat(rule.getExample(), is("123"));
    assertThat(rule.getHtmlDescription(), is("<p>abc</p><pre>123</pre>"));
  }
}
