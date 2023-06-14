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
package au.com.integradev.delphi.checks;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class MixedNamesCheckTest {
  @Test
  void testMatchingVarNamesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  MyVar: Boolean;")
                .appendImpl("begin")
                .appendImpl("  MyVar := True;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testMismatchedVarNamesShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  MyVar: Boolean;")
                .appendImpl("begin")
                .appendImpl("  myvar := True;")
                .appendImpl("end;"))
        .verifyIssueOnLine(11);
  }

  @Test
  void testQualifiedVarNamesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  MyVar: Boolean;")
                .appendImpl("begin")
                .appendImpl("  FMyField.myvar := True;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testMatchingFunctionNamesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class(TObject)")
                .appendDecl("  procedure DoThing(SomeArg: ArgType);")
                .appendDecl("end;")
                .appendImpl("procedure TFoo.DoThing(SomeArg: ArgType);")
                .appendImpl("begin")
                .appendImpl("  DoAnotherThing(SomeArg);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testMismatchedTypeNameShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class(TObject)")
                .appendDecl("  procedure DoThing(SomeArg: ArgType);")
                .appendDecl("end;")
                .appendImpl("procedure Tfoo.DoThing(SomeArg: ArgType);")
                .appendImpl("begin")
                .appendImpl("  DoAnotherThing(SomeArg);")
                .appendImpl("end;"))
        .verifyIssueOnLine(11);
  }

  @Test
  void testMismatchedFunctionNameShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class(TObject)")
                .appendDecl("  procedure DoThing(SomeArg: ArgType);")
                .appendDecl("end;")
                .appendImpl("procedure TFoo.doThing(SomeArg: ArgType);")
                .appendImpl("begin")
                .appendImpl("  DoAnotherThing(SomeArg);")
                .appendImpl("end;"))
        .verifyIssueOnLine(11);
  }

  @Test
  void testMismatchedExceptionNameShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .onFile(
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
                .appendImpl("end;"))
        .verifyIssueOnLine(13);
  }

  @Test
  void testMismatchedVarNameInAsmBlockShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .onFile(
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
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testMismatchedVarNameInAsmProcShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure; forward;")
                .appendImpl("procedure MyProcedure;")
                .appendImpl("var")
                .appendImpl("  MyArg: Integer;")
                .appendImpl("asm")
                .appendImpl("  MOV EAX, Myarg")
                .appendImpl("  ADD EAX, 2")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testSelfShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    procedure Bar;")
                .appendDecl("  end;")
                .appendImpl("procedure TFoo.Bar;")
                .appendImpl("begin")
                .appendImpl("  Self.Bar;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testPrimaryExpressionNameResolverBugShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    class procedure Finalise;")
                .appendDecl("  end;")
                .appendImpl("class procedure TFoo.Finalise;")
                .appendImpl("begin")
                .appendImpl("  TFoo(UnknownObject).Finalise;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testMismatchedUnitNameShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("uses")
                .appendDecl("  System.sysutils;"))
        .verifyIssueOnLine(6);
  }

  @Test
  void testMismatchedNamespaceNameShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("uses")
                .appendDecl("  system.SysUtils;"))
        .verifyIssueOnLine(6);
  }

  @Test
  void testMatchingUnitNameShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("uses")
                .appendDecl("  System.SysUtils;"))
        .verifyNoIssues();
  }

  @Test
  void testMatchingUnitNameWithoutUnitScopeShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .withUnitScopeName("System")
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("uses")
                .appendDecl("  SysUtils;"))
        .verifyNoIssues();
  }

  @Test
  void testMismatchedUnitNameWithoutUnitScopeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .withUnitScopeName("System")
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("uses")
                .appendDecl("  sysutils;"))
        .verifyIssueOnLine(6);
  }

  @Test
  void testUnitAliasShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .withUnitAlias("Foo", "System.SysUtils")
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("uses")
                .appendDecl("  Foo;"))
        .verifyNoIssues();
  }

  @Test
  void testMatchingUnitReferenceShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .withUnitScopeName("System")
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses SysUtils;")
                .appendImpl("procedure Proc;")
                .appendImpl("var")
                .appendImpl("  MyObject: TObject;")
                .appendImpl("begin")
                .appendImpl("  MyObject := TObject.Create;")
                .appendImpl("  SysUtils.FreeAndNil(MyObject);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnitReferenceMatchingDeclarationAndNotMatchingImportShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .withUnitScopeName("System")
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses sysutils;")
                .appendImpl("procedure Proc;")
                .appendImpl("var")
                .appendImpl("  MyObject: TObject;")
                .appendImpl("begin")
                .appendImpl("  MyObject := TObject.Create;")
                .appendImpl("  SysUtils.FreeAndNil(MyObject);")
                .appendImpl("end;"))
        .verifyIssueOnLine(5);
  }

  @Test
  void testMismatchedUnitReferenceShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MixedNamesCheck())
        .withUnitScopeName("System")
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses sysutils;")
                .appendImpl("procedure Proc;")
                .appendImpl("var")
                .appendImpl("  MyObject: TObject;")
                .appendImpl("begin")
                .appendImpl("  MyObject := TObject.Create;")
                .appendImpl("  sysutils.FreeAndNil(MyObject);")
                .appendImpl("end;"))
        .verifyIssueOnLine(5, 14);
  }

  private static DelphiTestUnitBuilder createSysUtils() {
    return new DelphiTestUnitBuilder()
        .unitName("System.SysUtils")
        .appendDecl("procedure FreeAndNil(var Obj); inline;");
  }
}
