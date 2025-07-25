/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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

class TooManyDefaultParametersCheckTest {
  @Test
  void testImplOnlyNoDefaultParamsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooManyDefaultParametersCheck())
        .onFile(
            new DelphiTestUnitBuilder()
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
        .verifyNoIssues();
  }

  @Test
  void testImplOnlySomeDefaultParamsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooManyDefaultParametersCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo(")
                .appendImpl("  Param1: Boolean;")
                .appendImpl("  Param2: Boolean;")
                .appendImpl("  Param3: Boolean;")
                .appendImpl("  Param4: Boolean;")
                .appendImpl("  Param5: Boolean;")
                .appendImpl("  Param6: Boolean;")
                .appendImpl("  Param7: Boolean = True;")
                .appendImpl("  Param8: Boolean = False")
                .appendImpl(");")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testImplMatchingDeclSomeDefaultParamsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooManyDefaultParametersCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo(")
                .appendDecl("  Param1: Boolean;")
                .appendDecl("  Param2: Boolean;")
                .appendDecl("  Param3: Boolean;")
                .appendDecl("  Param4: Boolean;")
                .appendDecl("  Param5: Boolean;")
                .appendDecl("  Param6: Boolean;")
                .appendDecl("  Param7: Boolean = True;")
                .appendDecl("  Param8: Boolean = False")
                .appendDecl(");")
                .appendImpl("procedure Foo(")
                .appendImpl("  Param1: Boolean;")
                .appendImpl("  Param2: Boolean;")
                .appendImpl("  Param3: Boolean;")
                .appendImpl("  Param4: Boolean;")
                .appendImpl("  Param5: Boolean;")
                .appendImpl("  Param6: Boolean;")
                .appendImpl("  Param7: Boolean = True;")
                .appendImpl("  Param8: Boolean = False")
                .appendImpl(");")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testImplNotMatchingDeclSomeDefaultParamsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooManyDefaultParametersCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo(")
                .appendDecl("  Param1: Boolean;")
                .appendDecl("  Param2: Boolean;")
                .appendDecl("  Param3: Boolean;")
                .appendDecl("  Param4: Boolean;")
                .appendDecl("  Param5: Boolean;")
                .appendDecl("  Param6: Boolean;")
                .appendDecl("  Param7: Boolean = True;")
                .appendDecl("  Param8: Boolean = False")
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
        .verifyNoIssues();
  }

  @Test
  void testImplOnlyTooManyDefaultParamsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooManyDefaultParametersCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo( // Noncompliant")
                .appendImpl("  Param1: Boolean;")
                .appendImpl("  Param2: Boolean;")
                .appendImpl("  Param3: Boolean;")
                .appendImpl("  Param4: Boolean = False;")
                .appendImpl("  Param5: Boolean = True;")
                .appendImpl("  Param6: Boolean = False;")
                .appendImpl("  Param7: Boolean = True;")
                .appendImpl("  Param8: Boolean = False")
                .appendImpl(");")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testImplMatchingDeclTooManyDefaultParamsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooManyDefaultParametersCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo( // Noncompliant")
                .appendDecl("  Param1: Boolean;")
                .appendDecl("  Param2: Boolean;")
                .appendDecl("  Param3: Boolean;")
                .appendDecl("  Param4: Boolean = False;")
                .appendDecl("  Param5: Boolean = True;")
                .appendDecl("  Param6: Boolean = False;")
                .appendDecl("  Param7: Boolean = True;")
                .appendDecl("  Param8: Boolean = False")
                .appendDecl(");")
                .appendImpl("procedure Foo(")
                .appendImpl("  Param1: Boolean;")
                .appendImpl("  Param2: Boolean;")
                .appendImpl("  Param3: Boolean;")
                .appendImpl("  Param4: Boolean = False;")
                .appendImpl("  Param5: Boolean = True;")
                .appendImpl("  Param6: Boolean = False;")
                .appendImpl("  Param7: Boolean = True;")
                .appendImpl("  Param8: Boolean = False")
                .appendImpl(");")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testImplNotMatchingDeclTooManyDefaultParamsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooManyDefaultParametersCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo( // Noncompliant")
                .appendDecl("  Param1: Boolean;")
                .appendDecl("  Param2: Boolean;")
                .appendDecl("  Param3: Boolean;")
                .appendDecl("  Param4: Boolean = False;")
                .appendDecl("  Param5: Boolean = True;")
                .appendDecl("  Param6: Boolean = False;")
                .appendDecl("  Param7: Boolean = True;")
                .appendDecl("  Param8: Boolean = False")
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
}
