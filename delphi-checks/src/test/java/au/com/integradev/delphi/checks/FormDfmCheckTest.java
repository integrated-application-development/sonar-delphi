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

class FormDfmCheckTest {
  @Test
  void testNormalClassWithoutDfmShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormDfmCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testAliasesWithoutDfmShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormDfmCheck())
        .withSearchPathUnit(createVclForms())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  Vcl.Forms;")
                .appendDecl("type")
                .appendDecl("  TFormWeakAlias = TForm;")
                .appendDecl("  TFrameWeakAlias = TFrame;")
                .appendDecl("  TFormStrongAlias = type TForm;")
                .appendDecl("  TFrameStrongAlias = type TFrame;"))
        .verifyNoIssues();
  }

  @Test
  void testFormWithDfmShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormDfmCheck())
        .withSearchPathUnit(createVclForms())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  Vcl.Forms;")
                .appendDecl("type")
                .appendDecl("  TFoo = class(TForm)")
                .appendDecl("  end;")
                .appendImpl("{$R Foo.dfm}"))
        .verifyNoIssues();
  }

  @Test
  void testFrameWithDfmShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormDfmCheck())
        .withSearchPathUnit(createVclForms())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  Vcl.Forms;")
                .appendDecl("type")
                .appendDecl("  TFoo = class(TFrame)")
                .appendDecl("  end;")
                .appendImpl("{$R Foo.dfm}"))
        .verifyNoIssues();
  }

  @Test
  void testFormWithoutDfmShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormDfmCheck())
        .withSearchPathUnit(createVclForms())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  Vcl.Forms;")
                .appendDecl("type")
                .appendDecl("  TFoo = class(TForm) // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testFrameWithoutDfmShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormDfmCheck())
        .withSearchPathUnit(createVclForms())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  Vcl.Forms;")
                .appendDecl("type")
                .appendDecl("  TFoo = class(TFrame) // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testNonDfmResourceShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormDfmCheck())
        .withSearchPathUnit(createVclForms())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  Vcl.Forms;")
                .appendDecl("type")
                .appendDecl("  TFoo = class(TForm) // Noncompliant")
                .appendDecl("  end;")
                .appendImpl("{$R Foo.bar}"))
        .verifyIssues();
  }

  @Test
  void testNonDfmResourceContainingDotDfmShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormDfmCheck())
        .withSearchPathUnit(createVclForms())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  Vcl.Forms;")
                .appendDecl("type")
                .appendDecl("  TFoo = class(TForm) // Noncompliant")
                .appendDecl("  end;")
                .appendImpl("{$R Foo.dfm.bar}"))
        .verifyIssues();
  }

  @Test
  void testIncludeDfmShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormDfmCheck())
        .withSearchPathUnit(createVclForms())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  Vcl.Forms;")
                .appendDecl("type")
                .appendDecl("  TFoo = class(TForm) // Noncompliant")
                .appendDecl("  end;")
                .appendImpl("{$I Foo.dfm}"))
        .verifyIssues();
  }

  private static DelphiTestUnitBuilder createVclForms() {
    return new DelphiTestUnitBuilder()
        .unitName("Vcl.Forms")
        .appendDecl("type")
        .appendDecl("  TForm = class")
        .appendDecl("  end;")
        .appendDecl("  TFrame = class")
        .appendDecl("  end;");
  }
}
