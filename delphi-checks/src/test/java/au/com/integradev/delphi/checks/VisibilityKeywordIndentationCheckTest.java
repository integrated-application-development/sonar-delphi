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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VisibilityKeywordIndentationCheckTest {
  @ParameterizedTest
  @ValueSource(
      strings = {
        "class",
        "record",
        "object",
        "class helper for TObject",
        "record helper for string"
      })
  void testTooIndentedVisibilitySpecifierShouldAddIssue(String structType) {
    CheckVerifier.newVerifier()
        .withCheck(new VisibilityKeywordIndentationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = " + structType)
                .appendDecl("    public // Noncompliant")
                .appendDecl("    procedure Proc;")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "class",
        "record",
        "object",
        "class helper for TObject",
        "record helper for string"
      })
  void testCorrectlyIndentedVisibilitySpecifierShouldNotAddIssue(String structType) {
    CheckVerifier.newVerifier()
        .withCheck(new VisibilityKeywordIndentationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = " + structType)
                .appendDecl("  protected")
                .appendDecl("    procedure Proc;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testImplicitPublishedVisibilitySectionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VisibilityKeywordIndentationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    procedure Bar;")
                .appendDecl("    procedure Baz;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnindentedVisibilitySpecifierShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VisibilityKeywordIndentationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("strict private // Noncompliant")
                .appendDecl("    procedure Proc;")
                .appendDecl("  end;"))
        .verifyIssues();
  }
}
