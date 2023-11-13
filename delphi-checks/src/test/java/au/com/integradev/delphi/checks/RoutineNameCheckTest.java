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

class RoutineNameCheckTest {
  @Test
  void testInterfaceMethodWithCompliantNameShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RoutineNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IMyInterface = interface")
                .appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testInterfaceMethodNameStartWithLowerCaseShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RoutineNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IMyInterface = interface")
                .appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']")
                .appendDecl("    procedure foo; // Noncompliant")
                .appendDecl("    function bar: Integer; // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testRoutineNameStartWithLowerCaseShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RoutineNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure foo; // Noncompliant")
                .appendDecl("function bar: Integer; // Noncompliant"))
        .verifyIssues();
  }

  @Test
  void testPublishedMethodsShouldBeSkipped() {
    CheckVerifier.newVerifier()
        .withCheck(new RoutineNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyForm = class(TForm)")
                .appendDecl("    procedure buttonOnClick(Sender: TNotifyEvent);")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testMethodsImplementingInterfacesWithMatchingNameShouldBeSkipped() {
    CheckVerifier.newVerifier()
        .withCheck(new RoutineNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IFoo = interface")
                .appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']")
                .appendDecl("    procedure invalidName; // Noncompliant")
                .appendDecl("  end;")
                .appendDecl("  TFoo = class(TObject, IFoo)")
                .appendDecl("    public")
                .appendDecl("      procedure invalidName;")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testMethodOverridesWithMatchingNameShouldBeSkipped() {
    CheckVerifier.newVerifier()
        .withCheck(new RoutineNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    public")
                .appendDecl("      procedure invalidName; // Noncompliant")
                .appendDecl("  end;")
                .appendDecl("  TBar = class(TFoo)")
                .appendDecl("    public")
                .appendDecl("      procedure invalidName; override;")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testMethodsImplementingInterfacesWithoutMatchingNameShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RoutineNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IFoo = interface")
                .appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']")
                .appendDecl("    procedure invalidname; // Noncompliant")
                .appendDecl("  end;")
                .appendDecl("  TFoo = class(TObject, IFoo)")
                .appendDecl("    public")
                .appendDecl("      procedure invalidName; // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testMethodOverridesWithoutMatchingNameShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RoutineNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    public")
                .appendDecl("      procedure invalidname; // Noncompliant")
                .appendDecl("  end;")
                .appendDecl("  TBar = class(TFoo)")
                .appendDecl("    public")
                .appendDecl("      procedure invalidName; override; // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testMethodOverridesWithUnknownTypeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RoutineNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(UNKNOWN_TYPE)")
                .appendDecl("    public")
                .appendDecl("      procedure invalidname; override; // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }
}
