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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.FatalAnalysisError;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

class InheritedTypeNameCheckTest {
  private static final String UNIT_NAME = "UnitName";

  private static DelphiCheck createCheck() {
    InheritedTypeNameCheck check = new InheritedTypeNameCheck();
    check.nameRegex = ".*_Child";
    check.parentTypeName = UNIT_NAME + ".Parent";
    return check;
  }

  @Test
  void testCompliesWithNamingConventionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .unitName(UNIT_NAME)
                .appendDecl("type")
                .appendDecl("  Parent = class")
                .appendDecl("  end;")
                .appendDecl("  TType_Child = class(Parent)")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testFailsNamingConventionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .unitName(UNIT_NAME)
                .appendDecl("type")
                .appendDecl("  Parent = class")
                .appendDecl("  end;")
                .appendDecl("  TType = class(Parent) // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testFailsNamingConventionWithMultipleParentsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .unitName(UNIT_NAME)
                .appendDecl("type")
                .appendDecl("  Parent = class")
                .appendDecl("  end;")
                .appendDecl("  TType = class(IType, Parent) // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testDoesNotInheritFromExpectedTypeShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .unitName(UNIT_NAME)
                .appendDecl("type")
                .appendDecl("  Parent = class")
                .appendDecl("  end;")
                .appendDecl("  TType = class(TObject)")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testDoesNotInheritFromAnyTypeShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .unitName(UNIT_NAME)
                .appendDecl("type")
                .appendDecl("  Parent = class")
                .appendDecl("  end;")
                .appendDecl("  TType = class")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testBadNameRegexShouldThrow() {
    InheritedTypeNameCheck check = new InheritedTypeNameCheck();
    check.parentTypeName = "Parent";
    check.nameRegex = "*";

    assertThatThrownBy(
            () ->
                CheckVerifier.newVerifier()
                    .withCheck(check)
                    .onFile(
                        new DelphiTestUnitBuilder()
                            .appendDecl("type")
                            .appendDecl("  Parent = class")
                            .appendDecl("  end;")
                            .appendDecl("  TType = class(Parent)")
                            .appendDecl("  end;"))
                    .verifyNoIssues())
        .isInstanceOf(FatalAnalysisError.class);
  }
}
