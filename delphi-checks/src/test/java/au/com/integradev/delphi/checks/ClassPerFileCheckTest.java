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

class ClassPerFileCheckTest {

  @Test
  void testOneClassShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ClassPerFileCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyClass = class(TObject)")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testForwardTypesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ClassPerFileCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyClass = class;")
                .appendDecl("  TMyClass = class(TObject)")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testStubTypesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ClassPerFileCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject);")
                .appendDecl("  TBar = class(TFoo);"))
        .verifyNoIssues();
  }

  @Test
  void testTwoClassesShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ClassPerFileCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyClass = class(TObject)")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;")
                .appendDecl("  TMyClass2 = class(TObject)")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;"))
        .verifyIssueOnFile();
  }

  @Test
  void testMultipleViolationsShouldAddOneIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ClassPerFileCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyClass = class(TObject)")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;")
                .appendDecl("  TMyClass2 = class(TObject)")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;")
                .appendDecl("  TMyClass3 = class(TObject)")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;"))
        .verifyIssueOnFile();
  }

  @Test
  void testFalsePositiveMetaClass() {
    CheckVerifier.newVerifier()
        .withCheck(new ClassPerFileCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyClass = class(TObject)")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;")
                .appendDecl("  TMetaClassClass = class of TMyClass;"))
        .verifyNoIssues();
  }

  @Test
  void testFalsePositiveClassMethods() {
    CheckVerifier.newVerifier()
        .withCheck(new ClassPerFileCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyClass = class(TObject)")
                .appendDecl("    class procedure TestProcedure;")
                .appendDecl("    class function TestFunction: Boolean;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }
}
