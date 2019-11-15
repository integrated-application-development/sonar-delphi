/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colbo
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

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class UnitNameRuleTest extends BasePmdRuleTest {

  @Test
  public void testValidRule() {
    execute(new DelphiTestUnitBuilder().unitName("TestUnits"));

    assertIssues().isEmpty();
  }

  @Test
  public void testValidUnitUsingNameSpace() {
    execute(new DelphiTestUnitBuilder().unitName("Namespaces.TestUnits"));

    assertIssues().isEmpty();
  }

  @Test
  public void testInvalidUnit() {
    execute(new DelphiTestUnitBuilder().unitName("myUnit"));

    assertIssues().hasSize(1).areExactly(1, ruleKeyAtLine("UnitNameRule", 1));
  }

  @Test
  public void testInvalidNamespace() {
    execute(new DelphiTestUnitBuilder().unitName("bad_Namespace.GoodUnit"));

    assertIssues().hasSize(1).areExactly(1, ruleKeyAtLine("UnitNameRule", 1));
  }

  @Test
  public void testInvalidUnitAndNameSpace() {
    execute(new DelphiTestUnitBuilder().unitName("bad_Namespace.SUPER_bad_UNIT"));

    assertIssues().hasSize(1).areExactly(1, ruleKeyAtLine("UnitNameRule", 1));
  }
}
