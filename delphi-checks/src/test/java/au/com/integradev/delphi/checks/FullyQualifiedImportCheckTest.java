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

import au.com.integradev.delphi.builders.DelphiTestProgramBuilder;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class FullyQualifiedImportCheckTest {

  @Test
  void testFullyQualifiedImportShouldNotAddIssue() {
    var importedUnit = new DelphiTestUnitBuilder().unitName("Scope.UnitU");

    var testFile = new DelphiTestUnitBuilder().appendDecl("uses").appendDecl("  Scope.UnitU;");

    CheckVerifier.newVerifier()
        .withCheck(new FullyQualifiedImportCheck())
        .withSearchPathUnit(importedUnit)
        .onFile(testFile)
        .verifyNoIssues();
  }

  @Test
  void testImportFullyQualifiedWithInKeywordShouldNotAddIssue() {
    var importedUnit = new DelphiTestUnitBuilder().unitName("Scope.UnitU");

    var testFile =
        new DelphiTestProgramBuilder()
            .appendDecl("uses")
            .appendDecl("  Scope.UnitU in 'Test.UnitU.pas';");

    CheckVerifier.newVerifier()
        .withCheck(new FullyQualifiedImportCheck())
        .withSearchPathUnit(importedUnit)
        .onFile(testFile)
        .verifyNoIssues();
  }

  @Test
  void testImportNotFullyQualifiedWithInKeywordShouldAddIssue() {
    var importedUnit = new DelphiTestUnitBuilder().unitName("Scope.UnitU");

    var testFile =
        new DelphiTestProgramBuilder()
            .appendDecl("uses")
            .appendDecl("  // Fix qf1@[+1:2 to +1:7] <<Scope.UnitU>>")
            .appendDecl("  UnitU in 'Test.UnitU.pas';  // Noncompliant");

    CheckVerifier.newVerifier()
        .withCheck(new FullyQualifiedImportCheck())
        .withSearchPathUnit(importedUnit)
        .onFile(testFile)
        .withUnitScopeName("Scope")
        .verifyIssues();
  }

  @Test
  void testUnresolvedImportShouldNotAddIssue() {
    var testFile = new DelphiTestUnitBuilder().appendDecl("uses").appendDecl("  RandomUnit;");

    CheckVerifier.newVerifier()
        .withCheck(new FullyQualifiedImportCheck())
        .onFile(testFile)
        .verifyNoIssues();
  }

  @Test
  void testUnitAliasImportShouldNotAddIssue() {
    var testFile = new DelphiTestUnitBuilder().appendDecl("uses").appendDecl("  AliasName;");

    CheckVerifier.newVerifier()
        .withCheck(new FullyQualifiedImportCheck())
        .withSearchPathUnit(new DelphiTestUnitBuilder().unitName("Scope.RealName"))
        .withSearchPathUnit(new DelphiTestUnitBuilder().unitName("Scope.AliasName"))
        .withUnitScopeName("Scope")
        .withUnitAlias("AliasName", "Scope.RealName")
        .onFile(testFile)
        .verifyNoIssues();
  }

  @Test
  void testUnitAliasImportThatLooksLikeUnqualifiedImportShouldNotAddIssue() {
    var testFile = new DelphiTestUnitBuilder().appendDecl("uses").appendDecl("  Name;");

    CheckVerifier.newVerifier()
        .withCheck(new FullyQualifiedImportCheck())
        .withSearchPathUnit(new DelphiTestUnitBuilder().unitName("Scope.Name"))
        .withUnitScopeName("Scope")
        .withUnitAlias("Name", "Scope.Name")
        .onFile(testFile)
        .verifyNoIssues();
  }

  @Test
  void testNotFullyQualifiedImportShouldAddIssue() {
    var importedUnit = new DelphiTestUnitBuilder().unitName("Scope.UnitU");

    var testFile =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("  // Fix qf1@[+1:2 to +1:7] <<Scope.UnitU>>")
            .appendDecl("  UnitU;  // Noncompliant");

    CheckVerifier.newVerifier()
        .withCheck(new FullyQualifiedImportCheck())
        .withSearchPathUnit(importedUnit)
        .onFile(testFile)
        .withUnitScopeName("Scope")
        .verifyIssues();
  }
}
