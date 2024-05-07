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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FormatArgumentTypeCheckTest {
  private DelphiTestUnitBuilder sysUtils() {
    return new DelphiTestUnitBuilder()
        .unitName("System.SysUtils")
        .appendDecl("function Format(const Format: string; const Args: array of const): string;");
  }

  @ParameterizedTest(name = "{0}: {1} adds issue")
  @CsvSource(
      value = {
        "%d,'a'",
        "%d,25.25",
        "%d,nil",
        "%f,'a'",
        "%f,25",
        "%f,nil",
        "%f,0",
        "%p,'a'",
        "%p,25",
        "%p,25.5",
        "%p,0",
        "%s,25",
        "%s,25.5",
        "%s,nil",
        "%s,0"
      },
      quoteCharacter = '`')
  void testInvalidTypeShouldAddIssue(String formatString, String argument) {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format('" + formatString + "', [")
                .appendImpl("    " + argument + " // Noncompliant")
                .appendImpl("  ]);"))
        .verifyIssues();
  }

  @ParameterizedTest(name = "{0}: {1} does not add issue")
  @CsvSource(
      value = {
        "%d,5000", "%d,-5000", "%d,0",
        "%u,5000", "%u,-5000", "%u,0",
        "%x,5000", "%x,-5000", "%x,0",
        "%e,25.5", "%e,-25.5", "%e,0.0",
        "%f,25.5", "%f,-25.5", "%f,0.0",
        "%g,25.5", "%g,-25.5", "%g,0.0",
        "%n,25.5", "%n,-25.5", "%n,0.0",
        "%m,25.5", "%m,-25.5", "%m,0.0",
        "%p,nil", "%p,@Format", "%p,Pointer(5)",
        "%s,'hello'", "%s,'e'", "%s,PChar('hello')",
      },
      quoteCharacter = '`')
  void testValidTypeShouldNotAddIssue(String formatString, String argument) {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format('" + formatString + "', [")
                .appendImpl("    " + argument)
                .appendImpl("  ]);"))
        .verifyNoIssues();
  }

  @Test
  void testOneInvalidArgumentShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format('My name is %s and I am %d years old', [")
                .appendImpl("    'Greg',")
                .appendImpl("    'Bob' // Noncompliant")
                .appendImpl("  ]);"))
        .verifyIssues();
  }

  @Test
  void testPartiallyValidShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format('My name is %s and I am %0:d years old', [")
                .appendImpl("    'Bob' // Noncompliant")
                .appendImpl("  ]);"))
        .verifyIssues();
  }

  @Test
  void testMultipleValidTypesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format('I got %*.*f and I am %0:d years old', [")
                .appendImpl("    5,")
                .appendImpl("    2,")
                .appendImpl("    67.455")
                .appendImpl("  ]);"))
        .verifyNoIssues();
  }

  @Test
  void testMultipleUppercaseValidTypesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format('I got %*.*F and I am %0:D years old', [")
                .appendImpl("    5,")
                .appendImpl("    2,")
                .appendImpl("    67.455")
                .appendImpl("  ]);"))
        .verifyNoIssues();
  }

  @Test
  void testInvalidFromWidthSpecifierShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format('I got %*.f and my name is %0:s', [")
                .appendImpl("    'Bob', // Noncompliant")
                .appendImpl("    75.5")
                .appendImpl("  ]);"))
        .verifyIssues();
  }

  @Test
  void testValidWidthSpecifierShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format('I got %*.f and my name is %s', [")
                .appendImpl("    6,")
                .appendImpl("    54.65,")
                .appendImpl("    'Bob'")
                .appendImpl("  ]);"))
        .verifyNoIssues();
  }

  @Test
  void testInvalidWidthSpecifierShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format('I got %*.f and my name is %s', [")
                .appendImpl("    30.33, // Noncompliant")
                .appendImpl("    54.65,")
                .appendImpl("    'Bob'")
                .appendImpl("  ]);"))
        .verifyIssues();
  }

  @Test
  void testValidPrecisionSpecifierShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format('I got %.*f and my name is %s', [")
                .appendImpl("    6,")
                .appendImpl("    54.65,")
                .appendImpl("    'Bob'")
                .appendImpl("  ]);"))
        .verifyNoIssues();
  }

  @Test
  void testInvalidPrecisionSpecifierShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format('I got %.*f and my name is %s', [")
                .appendImpl("    30.33, // Noncompliant")
                .appendImpl("    54.65,")
                .appendImpl("    'Bob'")
                .appendImpl("  ]);"))
        .verifyIssues();
  }

  @Test
  void testWrongNumberArgumentsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format('I got %.*f and my name is %s', [")
                .appendImpl("    2,")
                .appendImpl("    54.65")
                .appendImpl("  ]);"))
        .verifyNoIssues();
  }

  @Test
  void testStaticCharArrayForStringShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("var")
                .appendImpl("  MyArr: array[0..4] of Char = ('a', 'b', 'c', 'd', #0);")
                .appendImpl("initialization")
                .appendImpl("  Format('I got %s', [")
                .appendImpl("    MyArr")
                .appendImpl("  ]);"))
        .verifyNoIssues();
  }

  @Test
  void testDynamicCharArrayForStringShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("var")
                .appendImpl("  MyArr: array of Char = ['a', 'b', 'c', 'd', #0];")
                .appendImpl("initialization")
                .appendImpl("  Format('I got %s', [")
                .appendImpl("    MyArr // Noncompliant")
                .appendImpl("  ]);"))
        .verifyIssues();
  }

  @Test
  void testVariantForStringShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("var")
                .appendImpl("  MyVariant: Variant;")
                .appendImpl("initialization")
                .appendImpl("  Format('I got %s', [")
                .appendImpl("    MyVariant")
                .appendImpl("  ]);"))
        .verifyNoIssues();
  }

  @Test
  void testVariantForNonStringShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("var")
                .appendImpl("  MyVariant: Variant;")
                .appendImpl("initialization")
                .appendImpl("  Format('%d %n %x %p', [")
                .appendImpl("    MyVariant, // Noncompliant")
                .appendImpl("    MyVariant, // Noncompliant")
                .appendImpl("    MyVariant, // Noncompliant")
                .appendImpl("    MyVariant  // Noncompliant")
                .appendImpl("  ]);"))
        .verifyIssues();
  }

  @Test
  void testCorrectProcedureResultShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("function GetName: string;")
                .appendImpl("begin")
                .appendImpl("  Result := 'Ted';")
                .appendImpl("end;")
                .appendImpl("initialization")
                .appendImpl("  Format('I got %s', [")
                .appendImpl("    GetName")
                .appendImpl("  ]);"))
        .verifyNoIssues();
  }

  @Test
  void testUnfulfilledCorrectProcedureResultShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("function GetName(Last: Boolean): string;")
                .appendImpl("begin")
                .appendImpl("  Result := 'Ted';")
                .appendImpl("end;")
                .appendImpl("initialization")
                .appendImpl("  Format('I got %s', [")
                .appendImpl("    GetName // Noncompliant")
                .appendImpl("  ]);"))
        .verifyIssues();
  }

  @Test
  void testIncorrectProcedureResultShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentTypeCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("function GetAge: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := 30;")
                .appendImpl("end;")
                .appendImpl("initialization")
                .appendImpl("  Format('I got %s', [")
                .appendImpl("    GetAge // Noncompliant")
                .appendImpl("  ]);"))
        .verifyIssues();
  }
}
