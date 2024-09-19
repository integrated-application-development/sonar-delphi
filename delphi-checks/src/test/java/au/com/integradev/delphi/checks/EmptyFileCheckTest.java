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

import au.com.integradev.delphi.builders.DelphiTestFile;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class EmptyFileCheckTest {
  private static final String PACKAGE_FILE =
      "/au/com/integradev/delphi/checks/EmptyFileCheck/PackageFile.dpk";

  @Test
  void testEmptyUnitShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyFileCheck())
        .onFile(new DelphiTestUnitBuilder())
        .verifyIssueOnFile();
  }

  @Test
  void testEmptyUnitWithImportsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyFileCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("    Foo")
                .appendDecl("   , Bar")
                .appendDecl("   , Baz")
                .appendDecl("   ;"))
        .verifyIssueOnFile();
  }

  @Test
  void testRoutineDeclarationShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyFileCheck())
        .onFile(new DelphiTestUnitBuilder().appendDecl("procedure Foo;"))
        .verifyNoIssues();
  }

  @Test
  void testRoutineImplementationShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyFileCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testVariableDeclarationsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyFileCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("var")
                .appendDecl("  GFoo: TObject;")
                .appendDecl("  GBar: TObject;"))
        .verifyNoIssues();
  }

  @Test
  void testConstantDeclarationsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyFileCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("const")
                .appendDecl("  C_Foo = 123;")
                .appendDecl("  C_Bar = 456;"))
        .verifyNoIssues();
  }

  @Test
  void testTypeDeclarationShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyFileCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testPackageShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyFileCheck())
        .onFile(DelphiTestFile.fromResource(PACKAGE_FILE))
        .verifyNoIssues();
  }
}
