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
package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class UnusedPropertiesRuleTest extends BasePmdRuleTest {
  @Test
  void testUnusedPropertyShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class")
            .appendDecl("private")
            .appendDecl("  FBar: Integer;")
            .appendDecl("public")
            .appendDecl("  property Bar: Integer read FBar;")
            .appendDecl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("UnusedPropertiesRule", builder.getOffsetDecl() + 5));
  }

  @Test
  void testUsedInMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class")
            .appendDecl("private")
            .appendDecl("  FBar: Integer;")
            .appendDecl("public")
            .appendDecl("  property Bar: Integer read FBar;")
            .appendDecl("end;")
            .appendImpl("procedure Baz(Foo: TFoo);")
            .appendImpl("begin")
            .appendImpl("  Foo.Bar := 0;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedPropertiesRule"));
  }

  @Test
  void testUsedDefaultPropertyShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class")
            .appendDecl("public")
            .appendDecl("   property Items[Index: Integer]: TObject; default;")
            .appendDecl("end;")
            .appendImpl("function Baz(Foo: TFoo): TObject;")
            .appendImpl("begin")
            .appendImpl("  Result := Foo[0];")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedPropertiesRule"));
  }

  @Test
  void testUnusedPublishedPropertyShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class")
            .appendDecl("private")
            .appendDecl("  FBar: Integer;")
            .appendDecl("published")
            .appendDecl("  property Bar: Integer read FBar;")
            .appendDecl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedPropertiesRule"));
  }

  @Test
  void testUnusedRedeclaredPropertyShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class")
            .appendDecl("private")
            .appendDecl("  FBar: Integer;")
            .appendDecl("private")
            .appendDecl("  property Baz: Integer read FBar;")
            .appendDecl("end;")
            .appendDecl("type TBar = class(TFoo)")
            .appendDecl("public")
            .appendDecl("  property Baz;")
            .appendDecl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("UnusedPropertiesRule", builder.getOffsetDecl() + 5))
        .areExactly(1, ruleKeyAtLine("UnusedPropertiesRule", builder.getOffsetDecl() + 9));
  }

  @Test
  void testUnusedRedeclaredPublishedPropertyShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class")
            .appendDecl("private")
            .appendDecl("  FBar: Integer;")
            .appendDecl("private")
            .appendDecl("  property Baz: Integer read FBar;")
            .appendDecl("end;")
            .appendDecl("type TBar = class(TFoo)")
            .appendDecl("published")
            .appendDecl("  property Baz;")
            .appendDecl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedPropertiesRule"));
  }

  @Test
  void testUsedRedeclaredPropertyShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class")
            .appendDecl("private")
            .appendDecl("  FBar: Integer;")
            .appendDecl("private")
            .appendDecl("  property Baz: Integer read FBar;")
            .appendDecl("end;")
            .appendDecl("type TBar = class(TFoo)")
            .appendDecl("public")
            .appendDecl("  property Baz;")
            .appendDecl("end;")
            .appendImpl("function Flarp(Bar: TBar): Integer;")
            .appendImpl("begin")
            .appendImpl("  Result := Bar.Baz;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedPropertiesRule"));
  }

  @Test
  void testUnusedRedeclaredPropertyWithUsedConcretePropertyShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class")
            .appendDecl("private")
            .appendDecl("  FBar: Integer;")
            .appendDecl("private")
            .appendDecl("  property Baz: Integer read FBar;")
            .appendDecl("end;")
            .appendDecl("type TBar = class(TFoo)")
            .appendDecl("public")
            .appendDecl("  property Baz;")
            .appendDecl("end;")
            .appendImpl("function Flarp(Foo: TFoo): Integer;")
            .appendImpl("begin")
            .appendImpl("  Result := Bar.Baz;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("UnusedPropertiesRule", builder.getOffsetDecl() + 9));
  }
}
