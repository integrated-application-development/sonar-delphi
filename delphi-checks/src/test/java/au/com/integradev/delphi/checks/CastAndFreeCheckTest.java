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
                .appendImpl("  (Bar as TXyzz).Free;")
                .appendImpl("end;"))
        .verifyIssueOnLine(12);
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
                .appendImpl("procedure Foo(Bar: Baz);")
                .appendImpl("begin")
                .appendImpl("  (Bar as Xyzzy).Free;")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
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
                .appendImpl("  TObject(Bar).Free;")
                .appendImpl("end;"))
        .verifyIssueOnLine(11);
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
                .appendImpl("  FreeAndNil(Bar as TObject);")
                .appendImpl("end;"))
        .verifyIssueOnLine(11);
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
                .appendImpl("  FreeAndNil(TObject(Bar));")
                .appendImpl("end;"))
        .verifyIssueOnLine(11);
  }

  @Test
  void testNestedSoftCastFreeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CastAndFreeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo(Bar: Baz);")
                .appendImpl("begin")
                .appendImpl("  (((Bar as TObject))).Free;")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }

  @Test
  void testNestedHardCastFreeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CastAndFreeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo(Bar: Baz);")
                .appendImpl("begin")
                .appendImpl("  ((Xyzzy(Bar))).Free;")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
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
                .appendImpl("  FreeAndNil(((Bar as Xyzzy)));")
                .appendImpl("end;"))
        .verifyIssueOnLine(11);
  }

  @Test
  void testNestedHardCastFreeAndNilShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CastAndFreeCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("procedure Foo(Bar: Baz);")
                .appendImpl("begin")
                .appendImpl("  FreeAndNil(((Xyzzy(Bar))));")
                .appendImpl("end;"))
        .verifyIssueOnLine(11);
  }

  private static DelphiTestUnitBuilder createSysUtils() {
    return new DelphiTestUnitBuilder()
        .unitName("System.SysUtils")
        .appendDecl("procedure FreeAndNil(var Obj); inline;");
  }
}
