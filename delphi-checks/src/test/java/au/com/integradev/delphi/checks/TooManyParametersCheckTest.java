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

class TooManyParametersCheckTest {
  @Test
  void testImplOkParamsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooManyParametersCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo(Param: Boolean);")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testImplOnlyTooManyParamsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooManyParametersCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo( // Noncompliant")
                .appendImpl("  Param1: Boolean;")
                .appendImpl("  Param2: Boolean;")
                .appendImpl("  Param3: Boolean;")
                .appendImpl("  Param4: Boolean;")
                .appendImpl("  Param5: Boolean;")
                .appendImpl("  Param6: Boolean;")
                .appendImpl("  Param7: Boolean;")
                .appendImpl("  Param8: Boolean")
                .appendImpl(");")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testImplAndDeclTooManyParamsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooManyParametersCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo( // Noncompliant")
                .appendDecl("  Param1: Boolean;")
                .appendDecl("  Param2: Boolean;")
                .appendDecl("  Param3: Boolean;")
                .appendDecl("  Param4: Boolean;")
                .appendDecl("  Param5: Boolean;")
                .appendDecl("  Param6: Boolean;")
                .appendDecl("  Param7: Boolean;")
                .appendDecl("  Param8: Boolean")
                .appendDecl(");")
                .appendImpl("procedure Foo(")
                .appendImpl("  Param1: Boolean;")
                .appendImpl("  Param2: Boolean;")
                .appendImpl("  Param3: Boolean;")
                .appendImpl("  Param4: Boolean;")
                .appendImpl("  Param5: Boolean;")
                .appendImpl("  Param6: Boolean;")
                .appendImpl("  Param7: Boolean;")
                .appendImpl("  Param8: Boolean")
                .appendImpl(");")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testImplAndDeclOkParamsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooManyParametersCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo(")
                .appendDecl("  Param1: Boolean;")
                .appendDecl("  Param2: Boolean")
                .appendDecl(");")
                .appendImpl("procedure Foo(")
                .appendImpl("  Param1: Boolean;")
                .appendImpl("  Param2: Boolean")
                .appendImpl(");")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testDeclOnlyTooManyParamsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooManyParametersCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo( // Noncompliant")
                .appendDecl("  Param1: Boolean;")
                .appendDecl("  Param2: Boolean;")
                .appendDecl("  Param3: Boolean;")
                .appendDecl("  Param4: Boolean;")
                .appendDecl("  Param5: Boolean;")
                .appendDecl("  Param6: Boolean;")
                .appendDecl("  Param7: Boolean;")
                .appendDecl("  Param8: Boolean")
                .appendDecl(");"))
        .verifyIssues();
  }

  @Test
  void testInterfaceTooManyParamsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooManyParametersCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type IMyIntf = interface")
                .appendDecl("  procedure Foo( // Noncompliant")
                .appendDecl("    Param1: Boolean;")
                .appendDecl("    Param2: Boolean;")
                .appendDecl("    Param3: Boolean;")
                .appendDecl("    Param4: Boolean;")
                .appendDecl("    Param5: Boolean;")
                .appendDecl("    Param6: Boolean;")
                .appendDecl("    Param7: Boolean;")
                .appendDecl("    Param8: Boolean")
                .appendDecl("  );")
                .appendDecl("end;"))
        .verifyIssues();
  }

  @Test
  void testConstructorTooManyParamsShouldAddIssue() {
    var check = new TooManyParametersCheck();
    check.max = 9999;

    CheckVerifier.newVerifier()
        .withCheck(new TooManyParametersCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TMyObj = class(TObject)")
                .appendDecl("  constructor Create( // Noncompliant")
                .appendDecl("    Param1: Boolean;")
                .appendDecl("    Param2: Boolean;")
                .appendDecl("    Param3: Boolean;")
                .appendDecl("    Param4: Boolean;")
                .appendDecl("    Param5: Boolean;")
                .appendDecl("    Param6: Boolean;")
                .appendDecl("    Param7: Boolean;")
                .appendDecl("    Param8: Boolean")
                .appendDecl("  );")
                .appendDecl("end;"))
        .verifyIssues();
  }

  @Test
  void testNonConstructorTooManyParamsShouldAddIssue() {
    var check = new TooManyParametersCheck();
    check.constructorMax = 9999;

    CheckVerifier.newVerifier()
        .withCheck(new TooManyParametersCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TMyObj = class(TObject)")
                .appendDecl("  procedure Create( // Noncompliant")
                .appendDecl("    Param1: Boolean;")
                .appendDecl("    Param2: Boolean;")
                .appendDecl("    Param3: Boolean;")
                .appendDecl("    Param4: Boolean;")
                .appendDecl("    Param5: Boolean;")
                .appendDecl("    Param6: Boolean;")
                .appendDecl("    Param7: Boolean;")
                .appendDecl("    Param8: Boolean")
                .appendDecl("  );")
                .appendDecl("end;"))
        .verifyIssues();
  }
}
