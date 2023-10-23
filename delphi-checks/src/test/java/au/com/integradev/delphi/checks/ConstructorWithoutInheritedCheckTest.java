/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colombo
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

class ConstructorWithoutInheritedCheckTest {
  @Test
  void testConstructorWithInheritedShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConstructorWithoutInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TTestConstructor = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    constructor Create;")
                .appendDecl("  end;")
                .appendImpl("constructor TTestConstructor.Create;")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("  WriteLn('do something');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testConstructorMissingInheritedShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConstructorWithoutInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TTestConstructor = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    constructor Create;")
                .appendDecl("  end;")
                .appendImpl("constructor TTestConstructor.Create; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  WriteLn('do something');")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testClassConstructorShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConstructorWithoutInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TTestConstructor = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    class constructor Create;")
                .appendDecl("  end;")
                .appendImpl("class constructor TTestConstructor.Create;")
                .appendImpl("begin")
                .appendImpl("  WriteLn('do something');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRecordConstructorShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConstructorWithoutInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TTestRecord = record")
                .appendDecl("    FData : Integer;")
                .appendDecl("    constructor Create(Data : Integer);")
                .appendDecl("  end;")
                .appendImpl("constructor TTestRecord.Create(Data : Integer);")
                .appendImpl("begin")
                .appendImpl("  FData := Data;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testConstructorWithMissingTypeDeclarationShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConstructorWithoutInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("constructor TTestConstructor.Create; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  WriteLn('do something');")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
