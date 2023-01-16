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
package org.sonar.plugins.communitydelphi.pmd.rules;

import static org.sonar.plugins.communitydelphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.communitydelphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.pmd.xml.DelphiRule;
import org.sonar.plugins.communitydelphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.communitydelphi.utils.builders.DelphiTestUnitBuilder;

class UnusedImportsRuleTest extends BasePmdRuleTest {
  private final DelphiRuleProperty property = new DelphiRuleProperty("exclusions");

  @BeforeEach
  void setup() {
    DelphiRule rule = new DelphiRule();
    rule.setClazz("org.sonar.plugins.communitydelphi.pmd.rules.UnusedImportsRule");
    rule.setPriority(4);
    rule.setName("UnusedImportsRule_TEST");
    rule.setProperties(List.of(property));
    addRule(rule);
  }

  @Test
  void testUnusedImportShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("uses")
            .appendImpl("  System.SysUtils;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Obj: TObject;")
            .appendImpl("begin")
            .appendImpl("  Obj := TObject.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("UnusedImportsRule_TEST", builder.getOffset() + 2));
  }

  @Test
  void testUnresolvedImportShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("uses")
            .appendImpl("  NONEXISTENT_UNIT;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Obj: TObject;")
            .appendImpl("begin")
            .appendImpl("  Obj := TObject.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedImportsRule_TEST"));
  }

  @Test
  void testImplicitlyUsedImportShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("uses")
            .appendImpl("  System.SysUtils;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Obj: TObject;")
            .appendImpl("begin")
            .appendImpl("  Obj := TObject.Create;")
            .appendImpl("  FreeAndNil(Obj);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedImportsRule_TEST"));
  }

  @Test
  void testExcludedUnusedImportInInterfaceSectionShouldNotAddIssue() {
    property.setValue("System.SysUtils");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("  System.SysUtils;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Obj: TObject;")
            .appendImpl("begin")
            .appendImpl("  Obj := TObject.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedImportsRule_TEST"));
  }

  @Test
  void testExcludedUnusedImportInImplementationSectionShouldAddIssue() {
    property.setValue("System.SysUtils");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("uses")
            .appendImpl("  System.SysUtils;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Obj: TObject;")
            .appendImpl("begin")
            .appendImpl("  Obj := TObject.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("UnusedImportsRule_TEST", builder.getOffset() + 2));
  }
}
