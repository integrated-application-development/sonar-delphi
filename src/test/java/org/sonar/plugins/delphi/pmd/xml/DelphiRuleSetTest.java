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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Before;
import org.junit.Test;

public class DelphiRuleSetTest {

  private DelphiRuleSet ruleset;

  @Before
  public void init() {
    ruleset = new DelphiRuleSet();
  }

  @Test
  public void testName() {
    assertThat(ruleset.getName(), is(nullValue()));
    ruleset.setName("name");
    assertThat(ruleset.getName(), is("name"));
  }

  @Test
  public void testDescription() {
    assertThat(ruleset.getDescription(), is(nullValue()));
    ruleset.setDescription("desc");
    assertThat(ruleset.getDescription(), is("desc"));
  }

  @Test
  public void testRules() {
    assertThat(ruleset.getPmdRules(), is(empty()));
    assertThat(ruleset.getSonarRules(), is(empty()));

    DelphiRule pmdRule = new DelphiRule("testRule");
    ruleset.addRule(pmdRule);

    assertThat(ruleset.getPmdRules(), hasItems(pmdRule));
    assertThat(ruleset.getPmdRules(), IsCollectionWithSize.hasSize(1));
    assertThat(ruleset.getSonarRules(), IsCollectionWithSize.hasSize(1));
  }
}
