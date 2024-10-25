/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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

class MissingRaiseCheckTest {
  private DelphiTestUnitBuilder sysUtils() {
    return new DelphiTestUnitBuilder()
        .unitName("System.SysUtils")
        .appendDecl("type")
        .appendDecl("  Exception = class(TObject)")
        .appendDecl("  public")
        .appendDecl("    constructor Create(Text: string);")
        .appendDecl("  end;");
  }

  @Test
  void testDiscardedBaseExceptionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingRaiseCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Exception.Create('Error!'); // Noncompliant"))
        .verifyIssues();
  }

  @Test
  void testDiscardedDescendantExceptionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingRaiseCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses System.SysUtils;")
                .appendDecl("type")
                .appendDecl("  ECalculatorError = class(Exception)")
                .appendDecl("  public")
                .appendDecl("    constructor Create(A: Integer; B: Integer);")
                .appendDecl("  end;")
                .appendImpl("initialization")
                .appendImpl("  ECalculatorError.Create(1, 2); // Noncompliant"))
        .verifyIssues();
  }

  @Test
  void testDiscardedNonExceptionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingRaiseCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses System.SysUtils;")
                .appendDecl("type")
                .appendDecl("  EConfusinglyNamedNormalObject = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    constructor Create(A: Integer; B: Integer);")
                .appendDecl("  end;")
                .appendImpl("initialization")
                .appendImpl("  EConfusinglyNamedNormalObject.Create(1, 2);"))
        .verifyNoIssues();
  }

  @Test
  void testRaisedBaseExceptionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingRaiseCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  raise Exception.Create('Error!');"))
        .verifyNoIssues();
  }

  @Test
  void testDiscardedInvocationShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingRaiseCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("function Foo: Exception;")
                .appendImpl("begin")
                .appendImpl("end;")
                .appendImpl("initialization")
                .appendImpl("  Foo;"))
        .verifyNoIssues();
  }

  @Test
  void testDiscardedNonConstructorInvocationShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingRaiseCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses System.SysUtils;")
                .appendDecl("type")
                .appendDecl("  ECalculatorError = class(Exception)")
                .appendDecl("  public")
                .appendDecl("    class function Add(A: Integer; B: Integer);")
                .appendDecl("  end;")
                .appendImpl("initialization")
                .appendImpl("  ECalculatorError.Add(1, 2);"))
        .verifyNoIssues();
  }

  @Test
  void testRaisedDescendantExceptionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingRaiseCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses System.SysUtils;")
                .appendDecl("type")
                .appendDecl("  ECalculatorError = class(Exception)")
                .appendDecl("  public")
                .appendDecl("    constructor Create(A: Integer; B: Integer);")
                .appendDecl("  end;")
                .appendImpl("initialization")
                .appendImpl("  raise ECalculatorError.Create(1, 2);"))
        .verifyNoIssues();
  }

  @Test
  void testAssignedBaseExceptionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingRaiseCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("var MyError: ECalculatorError;")
                .appendImpl("initialization")
                .appendImpl("  MyError := Exception.Create('Error!');"))
        .verifyNoIssues();
  }

  @Test
  void testAssignedDescendantExceptionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingRaiseCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses System.SysUtils;")
                .appendDecl("type")
                .appendDecl("  ECalculatorError = class(Exception)")
                .appendDecl("  public")
                .appendDecl("    constructor Create(A: Integer; B: Integer);")
                .appendDecl("  end;")
                .appendImpl("var MyError: ECalculatorError;")
                .appendImpl("initialization")
                .appendImpl("  MyError := ECalculatorError.Create(1, 2);"))
        .verifyNoIssues();
  }
}
