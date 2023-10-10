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

import static org.assertj.core.api.Assertions.assertThat;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.SetTypeNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

class HelperNameCheckTest {
  private static DelphiCheck createCheck(String prefixes, String extendedTypePrefixes) {
    HelperNameCheck check = new HelperNameCheck();
    check.helperPrefixes = prefixes;
    check.extendedTypePrefixes = extendedTypePrefixes;
    return check;
  }

  @Test
  void testCombinedPrefixesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new HelperNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  TTFooHelper = class helper for TFoo")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testMismatchedPrefixesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck("Prefix", "Tx"))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TxFoo = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  PrefixFooHelper = class helper for TxFoo")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testExtendedTypePrefixesNotStrippedShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck("Th", "Tx"))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TxFoo = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  ThTxFooHelper = class helper for TxFoo")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testSinglePrefixShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new HelperNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  TFooHelper = class helper for TFoo")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testNoPrefixShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new HelperNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  FooHelper = class helper for TFoo // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testContainsOriginalNameShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new HelperNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  TAbcFooHelper = class helper for TFoo")
                .appendDecl("  end;")
                .appendDecl("  TFooAbcHelper = class helper for TFoo")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testNoOriginalNameShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new HelperNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  THelper = class helper for TFoo // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testOriginalNameInsideOtherWordShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new HelperNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  TFootHelper = class helper for TFoo // Noncompliant")
                .appendDecl("  end;")
                .appendDecl("  TfooHelper = class helper for TFoo // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testOriginalNameAnywhereShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new HelperNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  TFootFooHelper = class helper for TFoo")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testNotPascalCaseShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new HelperNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  TmyFooHelper = class helper for TFoo // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testNoSuffixShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new HelperNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  PrefixFoo = class helper for TFoo // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testStringHelperShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new HelperNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TStringHelper = record helper for string")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testFileHelperShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new HelperNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFileHelper = record helper for file")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testGetUnknownExtendedTypeSimpleName() {
    DelphiAst ast =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMySet = set of string;")
            .delphiFile()
            .getAst();

    SetTypeNode setTypeNode = ast.getFirstChildOfType(SetTypeNode.class);
    assertThat(HelperNameCheck.getExtendedTypeSimpleName(setTypeNode)).isNull();
  }
}
