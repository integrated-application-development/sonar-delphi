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

class MixedNamesRuleTest extends BasePmdRuleTest {

  @Test
  void testMatchingVarNamesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  MyVar: Boolean;")
            .appendImpl("begin")
            .appendImpl("  MyVar := True;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MixedNamesRule"));
  }

  @Test
  void testMismatchedVarNamesShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  MyVar: Boolean;")
            .appendImpl("begin")
            .appendImpl("  myvar := True;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("MixedNamesRule", builder.getOffset() + 5));
  }

  @Test
  void testQualifiedVarNamesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  MyVar: Boolean;")
            .appendImpl("begin")
            .appendImpl("  FMyField.myvar := True;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MixedNamesRule"));
  }

  @Test
  void testMatchingFunctionNamesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class(TObject)")
            .appendDecl("  procedure DoThing(SomeArg: ArgType);")
            .appendDecl("end;")
            .appendImpl("procedure TFoo.DoThing(SomeArg: ArgType);")
            .appendImpl("begin")
            .appendImpl("  DoAnotherThing(SomeArg);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MixedNamesRule"));
  }

  @Test
  void testMismatchedTypeNameShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class(TObject)")
            .appendDecl("  procedure DoThing(SomeArg: ArgType);")
            .appendDecl("end;")
            .appendImpl("procedure Tfoo.DoThing(SomeArg: ArgType);")
            .appendImpl("begin")
            .appendImpl("  DoAnotherThing(SomeArg);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("MixedNamesRule", builder.getOffset() + 1));
  }

  @Test
  void testMismatchedFunctionNameShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class(TObject)")
            .appendDecl("  procedure DoThing(SomeArg: ArgType);")
            .appendDecl("end;")
            .appendImpl("procedure TFoo.doThing(SomeArg: ArgType);")
            .appendImpl("begin")
            .appendImpl("  DoAnotherThing(SomeArg);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("MixedNamesRule", builder.getOffset() + 1));
  }

  @Test
  void testMismatchedExceptionNameShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    raise Exception.Create('Everything is on fire!');")
            .appendImpl("  except")
            .appendImpl("    on E: Exception do begin")
            .appendImpl("      e.Bar;")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("MixedNamesRule", builder.getOffset() + 7));
  }

  @Test
  void testMismatchedVarNameInAsmBlockShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure; forward;")
            .appendImpl("procedure MyProcedure;")
            .appendImpl("var")
            .appendImpl("  MyArg: Integer;")
            .appendImpl("begin")
            .appendImpl("  asm")
            .appendImpl("    MOV EAX, Myarg")
            .appendImpl("    ADD EAX, 2")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MixedNamesRule"));
  }

  @Test
  void testMismatchedVarNameInAsmProcShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure; forward;")
            .appendImpl("procedure MyProcedure;")
            .appendImpl("var")
            .appendImpl("  MyArg: Integer;")
            .appendImpl("asm")
            .appendImpl("  MOV EAX, Myarg")
            .appendImpl("  ADD EAX, 2")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MixedNamesRule"));
  }

  @Test
  void testSelfShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    procedure Bar;")
            .appendDecl("  end;")
            .appendImpl("procedure TFoo.Bar;")
            .appendImpl("begin")
            .appendImpl("  Self.Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MixedNamesRule"));
  }

  @Test
  void testPrimaryExpressionNameResolverBugShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    class procedure Finalise;")
            .appendDecl("  end;")
            .appendImpl("class procedure TFoo.Finalise;")
            .appendImpl("begin")
            .appendImpl("  TFoo(UnknownObject).Finalise;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MixedNamesRule"));
  }

  @Test
  void testMismatchedUnitNameShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendDecl("uses").appendDecl("  System.sysutils;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("MixedNamesRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testMismatchedNamespaceNameShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendDecl("uses").appendDecl("  system.SysUtils;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("MixedNamesRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testMatchingUnitNameShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendDecl("uses").appendDecl("  System.SysUtils;");

    execute(builder);

    assertIssues().areNot(ruleKey("MixedNamesRule"));
  }

  @Test
  void testMatchingUnitNameWithoutUnitScopeShouldNotAddIssue() {
    addUnitScopeName("System");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendDecl("uses").appendDecl("  SysUtils;");

    execute(builder);

    assertIssues().areNot(ruleKey("MixedNamesRule"));
  }

  @Test
  void testMismatchedUnitNameWithoutUnitScopeShouldAddIssue() {
    addUnitScopeName("System");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendDecl("uses").appendDecl("  sysutils;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("MixedNamesRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testUnitAliasShouldNotAddIssue() {
    addUnitAlias("Foo", "System.SysUtils");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendDecl("uses").appendDecl("  Foo;");

    execute(builder);

    assertIssues().areNot(ruleKey("MixedNamesRule"));
  }

  @Test
  void testMatchingUnitReferenceShouldNotAddIssue() {
    addUnitScopeName("System");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses SysUtils;")
            .appendImpl("procedure Proc;")
            .appendImpl("var")
            .appendImpl("  MyObject: TObject;")
            .appendImpl("begin")
            .appendImpl("  MyObject := TObject.Create;")
            .appendImpl("  SysUtils.FreeAndNil(MyObject);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MixedNamesRule"));
  }

  @Test
  void testUnitReferenceMatchingDeclarationAndNotMatchingImportShouldNotAddIssue() {
    addUnitScopeName("System");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses sysutils;")
            .appendImpl("procedure Proc;")
            .appendImpl("var")
            .appendImpl("  MyObject: TObject;")
            .appendImpl("begin")
            .appendImpl("  MyObject := TObject.Create;")
            .appendImpl("  SysUtils.FreeAndNil(MyObject);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("MixedNamesRule", builder.getOffset() + 6));
  }

  @Test
  void testMismatchedUnitReferenceShouldAddIssue() {
    addUnitScopeName("System");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses sysutils;")
            .appendImpl("procedure Proc;")
            .appendImpl("var")
            .appendImpl("  MyObject: TObject;")
            .appendImpl("begin")
            .appendImpl("  MyObject := TObject.Create;")
            .appendImpl("  sysutils.FreeAndNil(MyObject);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("MixedNamesRule", builder.getOffsetDecl() + 1))
        .areExactly(1, ruleKeyAtLine("MixedNamesRule", builder.getOffset() + 6));
  }
}
