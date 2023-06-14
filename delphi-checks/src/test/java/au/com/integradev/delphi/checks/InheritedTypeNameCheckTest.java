/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.FatalAnalysisError;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

class InheritedTypeNameCheckTest {
  private static DelphiCheck createCheck() {
    InheritedTypeNameCheck check = new InheritedTypeNameCheck();
    check.nameRegularExpression = ".*_Child";
    check.parentNameRegularExpression = ".*_Parent";
    return check;
  }

  @Test
  void testCompliesWithNamingConventionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TType_Child = class(TType_Parent)")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testFailsNamingConventionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TType = class(TType_Parent)")
                .appendDecl("  end;"))
        .verifyIssueOnLine(6);
  }

  @Test
  void testFailsNamingConventionWithMultipleParentsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TType = class(IType, TType_Parent)")
                .appendDecl("  end;"))
        .verifyIssueOnLine(6);
  }

  @Test
  void testDoesNotInheritFromExpectedTypeShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TType = class(TSomeOtherType)")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testDoesNotInheritFromAnyTypeShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TType = class(TObject)")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testBadNameRegexShouldThrow() {
    InheritedTypeNameCheck check = new InheritedTypeNameCheck();
    check.parentNameRegularExpression = ".*";
    check.nameRegularExpression = "*";

    assertThatThrownBy(
            () ->
                CheckVerifier.newVerifier()
                    .withCheck(check)
                    .onFile(
                        new DelphiTestUnitBuilder()
                            .appendDecl("type")
                            .appendDecl("  TType = class(TType_Parent)")
                            .appendDecl("  end;"))
                    .verifyNoIssues())
        .isInstanceOf(FatalAnalysisError.class);
  }

  @Test
  void testBadParentRegexShouldNotAddIssue() {
    InheritedTypeNameCheck check = new InheritedTypeNameCheck();
    check.parentNameRegularExpression = "*";
    check.nameRegularExpression = ".*";

    assertThatThrownBy(
            () ->
                CheckVerifier.newVerifier()
                    .withCheck(check)
                    .onFile(
                        new DelphiTestUnitBuilder()
                            .appendDecl("type")
                            .appendDecl("  TType = class(TType_Parent)")
                            .appendDecl("  end;"))
                    .verifyNoIssues())
        .isInstanceOf(FatalAnalysisError.class);
  }
}
