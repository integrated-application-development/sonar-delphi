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

class FormFmxCheckTest {
  @Test
  void testNormalClassWithoutFmxShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormFmxCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testAliasesWithoutFmxShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormFmxCheck())
        .withSearchPathUnit(createFmxForms())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  FMX.Forms;")
                .appendDecl("type")
                .appendDecl("  TFormWeakAlias = TForm;")
                .appendDecl("  TFrameWeakAlias = TFrame;")
                .appendDecl("  TFormStrongAlias = type TForm;")
                .appendDecl("  TFrameStrongAlias = type TFrame;"))
        .verifyNoIssues();
  }

  @Test
  void testFormWithFmxShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormFmxCheck())
        .withSearchPathUnit(createFmxForms())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  FMX.Forms;")
                .appendDecl("type")
                .appendDecl("  TFoo = class(TForm)")
                .appendDecl("  end;")
                .appendImpl("{$R Foo.fmx}"))
        .verifyNoIssues();
  }

  @Test
  void testFrameWithFmxShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormFmxCheck())
        .withSearchPathUnit(createFmxForms())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  FMX.Forms;")
                .appendDecl("type")
                .appendDecl("  TFoo = class(TFrame)")
                .appendDecl("  end;")
                .appendImpl("{$R Foo.fmx}"))
        .verifyNoIssues();
  }

  @Test
  void testFormWithoutFmxShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormFmxCheck())
        .withSearchPathUnit(createFmxForms())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  FMX.Forms;")
                .appendDecl("type")
                .appendDecl("  TFoo = class(TForm) // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testFrameWithoutFmxShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormFmxCheck())
        .withSearchPathUnit(createFmxForms())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  FMX.Forms;")
                .appendDecl("type")
                .appendDecl("  TFoo = class(TFrame) // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testNonFmxResourceShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormFmxCheck())
        .withSearchPathUnit(createFmxForms())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  FMX.Forms;")
                .appendDecl("type")
                .appendDecl("  TFoo = class(TForm) // Noncompliant")
                .appendDecl("  end;")
                .appendImpl("{$R Foo.bar}"))
        .verifyIssues();
  }

  @Test
  void testNonFmxResourceContainingDotFmxShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormFmxCheck())
        .withSearchPathUnit(createFmxForms())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  FMX.Forms;")
                .appendDecl("type")
                .appendDecl("  TFoo = class(TForm) // Noncompliant")
                .appendDecl("  end;")
                .appendImpl("{$R Foo.fmx.bar}"))
        .verifyIssues();
  }

  @Test
  void testIncludeFmxShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormFmxCheck())
        .withSearchPathUnit(createFmxForms())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  FMX.Forms;")
                .appendDecl("type")
                .appendDecl("  TFoo = class(TForm) // Noncompliant")
                .appendDecl("  end;")
                .appendImpl("{$I Foo.fmx}"))
        .verifyIssues();
  }

  private static DelphiTestUnitBuilder createFmxForms() {
    return new DelphiTestUnitBuilder()
        .unitName("FMX.Forms")
        .appendDecl("type")
        .appendDecl("  TForm = class")
        .appendDecl("  end;")
        .appendDecl("  TFrame = class")
        .appendDecl("  end;");
  }
}
