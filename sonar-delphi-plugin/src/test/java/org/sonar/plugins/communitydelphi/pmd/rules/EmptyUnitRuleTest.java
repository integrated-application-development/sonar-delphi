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

import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.pmd.FilePosition;
import org.sonar.plugins.communitydelphi.utils.builders.DelphiTestFileBuilder;
import org.sonar.plugins.communitydelphi.utils.builders.DelphiTestFileBuilder.ResourceBuilder;
import org.sonar.plugins.communitydelphi.utils.builders.DelphiTestUnitBuilder;

class EmptyUnitRuleTest extends BasePmdRuleTest {
  @Test
  void testEmptyUnitShouldAddIssue() {
    execute(new DelphiTestUnitBuilder());
    assertIssues().areExactly(1, ruleKeyAtLine("EmptyUnitRule", FilePosition.UNDEFINED_LINE));
  }

  @Test
  void testEmptyUnitWithImportsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    Foo")
            .appendDecl("   , Bar")
            .appendDecl("   , Baz")
            .appendDecl("   ;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("EmptyUnitRule", FilePosition.UNDEFINED_LINE));
  }

  @Test
  void testMethodDeclarationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendDecl("procedure Foo;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyUnitRule"));
  }

  @Test
  void testMethodImplementationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyUnitRule"));
  }

  @Test
  void testVariableDeclarationsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("var")
            .appendDecl("  GFoo: TObject;")
            .appendDecl("  GBar: TObject;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyUnitRule"));
  }

  @Test
  void testConstantDeclarationsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("const")
            .appendDecl("  C_Foo = 123;")
            .appendDecl("  C_Bar = 456;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyUnitRule"));
  }

  @Test
  void testTypeDeclarationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyUnitRule"));
  }

  @Test
  void testIgnorePackage() {
    String dpkResourcePath = "/org/sonar/plugins/delphi/projects/SimpleProject/dpk/TestLib.dpk";
    DelphiTestFileBuilder<ResourceBuilder> builder =
        DelphiTestFileBuilder.fromResource(dpkResourcePath);

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyUnitRule"));
  }
}
