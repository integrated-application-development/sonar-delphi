/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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

class CastAndFreeCheckTest {
  @Test
  void testRegularFreeShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CastAndFreeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo(Bar: Baz);")
                .appendImpl("begin")
                .appendImpl("  Bar.Free;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testMethodCallShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CastAndFreeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  System.SysUtils;")
                .appendImpl("function Flarp(Bar: TObject): TObject;")
                .appendImpl("begin")
                .appendImpl("  Result := Bar;")
                .appendImpl("end;")
                .appendImpl("procedure Foo(Bar: TObject);")
                .appendImpl("begin")
                .appendImpl("  FreeAndNil(Flarp(Bar));")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testPointerToObjectCastShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CastAndFreeCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  System.SysUtils;")
                .appendImpl("procedure Foo(Bar: Pointer);")
                .appendImpl("begin")
                .appendImpl("  (TObject(Bar)).Free;")
                .appendImpl("  FreeAndNil(TObject(Bar));")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUntypedToObjectCastShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CastAndFreeCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  System.SysUtils;")
                .appendImpl("procedure Foo(Bar);")
                .appendImpl("begin")
                .appendImpl("  (TObject(Bar)).Free;")
                .appendImpl("  FreeAndNil(TObject(Bar));")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testWeirdPointerToObjectCastShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CastAndFreeCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  System.SysUtils;")
                .appendDecl("type")
                .appendDecl("  TList = class(TObject)")
                .appendDecl("  property Default[Index: Integer]: Pointer; default;")
                .appendDecl("end;")
                .appendImpl("procedure Foo(List: TList);")
                .appendImpl("begin")
                .appendImpl("  (TList(List[0])).Free;")
                .appendImpl("  FreeAndNil(TObject(List[0]));")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testWeirdSoftCastFreeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CastAndFreeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TXyzz = class(TObject) end;")
                .appendImpl("procedure Foo(Bar: Baz);")
                .appendImpl("begin")
                .appendImpl("  (Bar as TXyzz).Free; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testRegularFreeAndNilShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CastAndFreeCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("procedure Foo(Bar: Baz);")
                .appendImpl("begin")
                .appendImpl("  FreeAndNil(Bar);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testSoftCastFreeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CastAndFreeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBaz = class(TObject)")
                .appendDecl("  end;")
                .appendImpl("procedure Foo(Bar: TObject);")
                .appendImpl("begin")
                .appendImpl("  (Bar as TBaz).Free; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testHardCastFreeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CastAndFreeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("procedure Foo(Bar: Baz);")
                .appendImpl("begin")
                .appendImpl("  TObject(Bar).Free; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testSoftCastFreeAndNilShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CastAndFreeCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("procedure Foo(Bar: Baz);")
                .appendImpl("begin")
                .appendImpl("  FreeAndNil(Bar as TObject); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testHardCastFreeAndNilShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CastAndFreeCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("procedure Foo(Bar: Baz);")
                .appendImpl("begin")
                .appendImpl("  FreeAndNil(TObject(Bar)); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNestedSoftCastFreeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CastAndFreeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo(Bar: Baz);")
                .appendImpl("begin")
                .appendImpl("  (((Bar as TObject))).Free; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNestedHardCastFreeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CastAndFreeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  Flarp = class(TObject)")
                .appendDecl("  end;")
                .appendImpl("procedure Foo(Bar: Baz);")
                .appendImpl("begin")
                .appendImpl("  ((Flarp(Bar))).Free; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNestedSoftCastFreeAndNilShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CastAndFreeCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("procedure Foo(Bar: Baz);")
                .appendImpl("begin")
                .appendImpl("  FreeAndNil(((Bar as Xyzzy))); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNestedHardCastFreeAndNilShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CastAndFreeCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  Flarp = class(TObject)")
                .appendDecl("  end;")
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("procedure Foo(Bar: Baz);")
                .appendImpl("begin")
                .appendImpl("  FreeAndNil(((Flarp(Bar)))); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  private static DelphiTestUnitBuilder createSysUtils() {
    return new DelphiTestUnitBuilder()
        .unitName("System.SysUtils")
        .appendDecl("procedure FreeAndNil(var Obj); inline;");
  }
}
