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
package au.com.integradev.delphi.pmd.rules;

import static au.com.integradev.delphi.pmd.FilePosition.UNDEFINED_LINE;
import static au.com.integradev.delphi.utils.conditions.RuleKey.ruleKey;
import static au.com.integradev.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.utils.builders.DelphiTestUnitBuilder;
import org.junit.jupiter.api.Test;

class ClassPerFileRuleTest extends BasePmdRuleTest {

  @Test
  void testOneClassShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyClass = class(TObject)")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ClassPerFileRule"));
  }

  @Test
  void testForwardTypesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyClass = class;")
            .appendDecl("  TMyClass = class(TObject)")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ClassPerFileRule"));
  }

  @Test
  void testStubTypesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject);")
            .appendDecl("  TBar = class(TFoo);");

    execute(builder);

    assertIssues().areNot(ruleKey("ClassPerFileRule"));
  }

  @Test
  void testTwoClassesShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyClass = class(TObject)")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;")
            .appendDecl("  TMyClass2 = class(TObject)")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("ClassPerFileRule", UNDEFINED_LINE));
  }

  @Test
  void testMultipleViolationsShouldAddOneIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyClass = class(TObject)")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;")
            .appendDecl("  TMyClass2 = class(TObject)")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;")
            .appendDecl("  TMyClass3 = class(TObject)")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("ClassPerFileRule", UNDEFINED_LINE));
  }

  @Test
  void testFalsePositiveMetaClass() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyClass = class(TObject)")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;")
            .appendDecl("  TMetaClassClass = class of TMyClass;");

    execute(builder);

    assertIssues().areNot(ruleKey("ClassPerFileRule"));
  }

  @Test
  void testFalsePositiveClassMethods() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyClass = class(TObject)")
            .appendDecl("    class procedure TestProcedure;")
            .appendDecl("    class function TestFunction: Boolean;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ClassPerFileRule"));
  }
}
