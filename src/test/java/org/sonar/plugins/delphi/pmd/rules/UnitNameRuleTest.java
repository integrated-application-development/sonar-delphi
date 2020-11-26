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

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class UnitNameRuleTest extends BasePmdRuleTest {

  @BeforeEach
  void setup() {
    DelphiRuleProperty property =
        Objects.requireNonNull(
            getRule(UnitNameRule.class).getProperty(UnitNameRule.PREFIXES.name()));
    property.setValue("Prefix");
  }

  @Test
  void testValidRule() {
    execute(new DelphiTestUnitBuilder().unitName("PrefixTestUnits"));

    assertIssues().isEmpty();
  }

  @Test
  void testValidUnitUsingNameSpace() {
    execute(new DelphiTestUnitBuilder().unitName("Namespace.PrefixTestUnits"));

    assertIssues().isEmpty();
  }

  @Test
  void testInvalidUnit() {
    execute(new DelphiTestUnitBuilder().unitName("myUnit"));

    assertIssues().hasSize(1).areExactly(1, ruleKeyAtLine("UnitNameRule", 1));
  }
}
