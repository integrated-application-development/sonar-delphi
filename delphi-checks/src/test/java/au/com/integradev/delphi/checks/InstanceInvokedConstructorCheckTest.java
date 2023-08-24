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

class InstanceInvokedConstructorCheckTest {
  @Test
  void testConstructorInvokedOnObjectShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InstanceInvokedConstructorCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Obj: TObject;")
                .appendImpl("begin")
                .appendImpl("  Obj.Create;")
                .appendImpl("end;"))
        .verifyIssueOnLine(11);
  }

  @Test
  void testConstructorInvokedOnTypeIdentifierShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InstanceInvokedConstructorCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Obj: TObject;")
                .appendImpl("begin")
                .appendImpl("  Obj := TObject.Create;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testConstructorInvokedOnClassReferenceShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InstanceInvokedConstructorCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Obj: TObject;")
                .appendImpl("  Clazz: TClass;")
                .appendImpl("begin")
                .appendImpl("  Obj := Clazz.Create;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testConstructorInvokedOnSelfShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InstanceInvokedConstructorCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    constructor Create;")
                .appendDecl("    procedure Test;")
                .appendDecl("  end;")
                .appendImpl("procedure TFoo.Test;")
                .appendImpl("begin")
                .appendImpl("  Self.Create;")
                .appendImpl("  Create;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testBareInheritedShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InstanceInvokedConstructorCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    constructor Create;")
                .appendDecl("  end;")
                .appendImpl("constructor TFoo.Create;")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNamedInheritedShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InstanceInvokedConstructorCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    constructor Create;")
                .appendDecl("  end;")
                .appendImpl("constructor TFoo.Create;")
                .appendImpl("begin")
                .appendImpl("  inherited Create;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testQualifiedInheritedShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InstanceInvokedConstructorCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    constructor Create;")
                .appendDecl("  end;")
                .appendImpl("constructor TFoo.Create;")
                .appendImpl("begin")
                .appendImpl("  inherited.Create;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
