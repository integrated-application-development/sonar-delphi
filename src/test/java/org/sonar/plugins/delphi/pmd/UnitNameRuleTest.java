/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colombo
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
package org.sonar.plugins.delphi.pmd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;

public class UnitNameRuleTest extends BasePmdRuleTest {

  @Test
  public void testValidRule() {
    execute(new DelphiUnitBuilderTest().unitName("TestUnits"));

    assertThat(stringifyIssues(), issues, is(empty()));
  }

  @Test
  public void testValidUnitUsingNameSpace() {
    execute(new DelphiUnitBuilderTest().unitName("Namespaces.TestUnits"));

    assertThat(stringifyIssues(), issues, is(empty()));
  }

  @Test
  public void testInvalidUnit() {
    execute(new DelphiUnitBuilderTest().unitName("myUnit"));

    assertThat(stringifyIssues(), issues, hasSize(1));
    assertThat(stringifyIssues(), issues, hasItem(hasRuleKeyAtLine("UnitNameRule", 1)));
  }

  @Test
  public void testInvalidNamespace() {
    execute(new DelphiUnitBuilderTest().unitName("bad_Namespace.omGoodUnit"));

    assertThat(stringifyIssues(), issues, hasSize(1));
    assertThat(stringifyIssues(), issues, hasItem(hasRuleKeyAtLine("UnitNameRule", 1)));
  }

  @Test
  public void testInvalidUnitAndNameSpace() {
    execute(new DelphiUnitBuilderTest().unitName("bad_Namespace.SUPER_bad_UNIT"));

    assertThat(stringifyIssues(), issues, hasSize(2));
    assertThat(stringifyIssues(), issues, everyItem(hasRuleKeyAtLine("UnitNameRule", 1)));
  }
}
