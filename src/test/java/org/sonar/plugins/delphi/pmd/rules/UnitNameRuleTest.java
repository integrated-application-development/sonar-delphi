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
package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class UnitNameRuleTest extends BasePmdRuleTest {

  @Test
  public void testValidRule() {
    execute(new DelphiTestUnitBuilder().unitName("TestUnits"));

    assertIssues(empty());
  }

  @Test
  public void testValidUnitUsingNameSpace() {
    execute(new DelphiTestUnitBuilder().unitName("Namespaces.TestUnits"));

    assertIssues(empty());
  }

  @Test
  public void testInvalidUnit() {
    execute(new DelphiTestUnitBuilder().unitName("myUnit"));

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("UnitNameRule", 1)));
  }

  @Test
  public void testInvalidNamespace() {
    execute(new DelphiTestUnitBuilder().unitName("bad_Namespace.omGoodUnit"));

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("UnitNameRule", 1)));
  }

  @Test
  public void testInvalidUnitAndNameSpace() {
    execute(new DelphiTestUnitBuilder().unitName("bad_Namespace.SUPER_bad_UNIT"));

    assertIssues(hasSize(2));
    assertIssues(everyItem(hasRuleKeyAtLine("UnitNameRule", 1)));
  }
}
