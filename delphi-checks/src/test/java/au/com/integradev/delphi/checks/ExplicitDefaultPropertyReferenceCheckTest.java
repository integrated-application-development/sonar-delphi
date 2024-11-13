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

class ExplicitDefaultPropertyReferenceCheckTest {
  @Test
  void testImplicitDefaultPropertyAccessShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExplicitDefaultPropertyReferenceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("    function GetBar: TObject;")
                .appendDecl("    property Bar: TObject read GetBar; default;")
                .appendDecl("  end;")
                .appendImpl("procedure Test(Foo: TFoo);")
                .appendImpl("var")
                .appendImpl("  Bar: TObject;")
                .appendImpl("begin")
                .appendImpl("  Bar := Foo[0];")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testExplicitDefaultPropertyAccessShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExplicitDefaultPropertyReferenceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("    function GetBar: TObject;")
                .appendDecl("    property Bar[Index: Integer]: TObject read GetBar; default;")
                .appendDecl("  end;")
                .appendImpl("procedure Test(Foo: TFoo);")
                .appendImpl("var")
                .appendImpl("  Bar: TObject;")
                .appendImpl("begin")
                .appendImpl("  Bar := Foo.Bar[0]; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testExplicitDefaultPropertyAccessOnSelfShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExplicitDefaultPropertyReferenceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("    function GetBar: TObject;")
                .appendDecl("    procedure Test(Foo: TFoo);")
                .appendDecl("    property Bar[Index: Integer]: TObject read GetBar; default;")
                .appendDecl("  end;")
                .appendImpl("procedure TFoo.Test(Foo: TFoo);")
                .appendImpl("var")
                .appendImpl("  Obj: TObject;")
                .appendImpl("begin")
                .appendImpl("  Obj := Bar[0];")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testExplicitDefaultPropertyAccessOnOverloadedParentPropertyShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExplicitDefaultPropertyReferenceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("    property Baz[Index: Integer]: TObject; default;")
                .appendDecl("  end;")
                .appendDecl("  TBar = class(TFoo)")
                .appendDecl("    property Baz[Name: string]: TObject; default;")
                .appendDecl("  end;")
                .appendImpl("procedure Test(Bar: TBar);")
                .appendImpl("var")
                .appendImpl("  Obj: TObject;")
                .appendImpl("begin")
                .appendImpl("  Obj := Bar.Baz[0]; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
