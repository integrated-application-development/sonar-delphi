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
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class DelphiRuleTest {

  private DelphiRule rule;

  @Before
  public void init() {
    rule = new DelphiRule("class", "critical");
  }

  @Test
  public void testName() {
    assertEquals(null, rule.getName());
    rule.setName("test");
    assertEquals("test", rule.getName());
  }

  @Test
  public void testClazz() {
    assertEquals("class", rule.getClazz());
  }

  @Test
  public void testProperties() {
    assertNull(rule.getProperties());

    List<Property> properties = new ArrayList<>();
    properties.add(new Property("a", "b"));
    rule.setProperties(properties);

    assertEquals(properties, rule.getProperties());
    assertEquals(1, rule.getProperties().size());

    rule.addProperty(new Property("c", "d"));
    assertEquals(2, rule.getProperties().size());

  }

  @Test
  public void testCompareTo() {
    DelphiRule someOtherRule = new DelphiRule("not-class");
    DelphiRule comparableRule = new DelphiRule("class");

    assertNotEquals(0, rule.compareTo(someOtherRule));
    assertEquals(0, rule.compareTo(comparableRule));
  }

  @Test
  public void testPriority() {
    assertEquals("critical", rule.getPriority());
    assertNull(new DelphiRule("class").getPriority());
  }

  @Test
  public void testMessage() {
    rule.setMessage("my message");
    assertEquals("my message", rule.getMessage());
  }

  @Test
  public void testDescription() {
    assertEquals("", rule.getDescription());
  }

  @Test
  public void testCategory() {
    rule.setTag("bug,size");
    assertThat(rule.getTags(), Matchers.arrayContaining("bug", "size"));
  }

  @Test
  public void testEmptyCategory() {
    assertThat(rule.getTags(), is(emptyArray()));
  }

}
