/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package au.com.integradev.delphi.checks.verifier;

import static org.assertj.core.api.Assertions.*;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import org.junit.jupiter.api.Test;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.FileHeaderNode;
import org.sonar.plugins.communitydelphi.api.ast.ImplementationSectionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;

class CheckVerifierImplTest {
  private static final String MESSAGE = "Test message";

  @Test
  void testLineIssue() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseLineIssueOnFileHeaderCheck())
            .onFile(new DelphiTestUnitBuilder());

    assertThatCode(() -> verifier.verifyIssueOnLine(1)).doesNotThrowAnyException();

    assertThatThrownBy(verifier::verifyIssueOnFile).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyIssueOnProject).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyNoIssues).isInstanceOf(AssertionError.class);
  }

  @Test
  void testLineIssues() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseLineIssueOnFileHeaderAndImplementationCheck())
            .onFile(new DelphiTestUnitBuilder());

    assertThatCode(() -> verifier.verifyIssueOnLine(1, 5)).doesNotThrowAnyException();

    assertThatThrownBy(() -> verifier.verifyIssueOnLine(1)).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> verifier.verifyIssueOnLine(5)).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyIssueOnFile).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyIssueOnProject).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyNoIssues).isInstanceOf(AssertionError.class);
  }

  @Test
  void testFileIssue() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseFileIssueCheck())
            .onFile(new DelphiTestUnitBuilder());

    assertThatCode(verifier::verifyIssueOnFile).doesNotThrowAnyException();

    assertThatThrownBy(() -> verifier.verifyIssueOnLine(1)).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyIssueOnProject).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyNoIssues).isInstanceOf(AssertionError.class);
  }

  @Rule(key = "WillRaiseLineIssueOnFileHeader")
  public static class WillRaiseLineIssueOnFileHeaderCheck extends DelphiCheck {
    @Override
    public DelphiCheckContext visit(FileHeaderNode fileHeader, DelphiCheckContext context) {
      reportIssue(context, fileHeader, MESSAGE);
      return super.visit(fileHeader, context);
    }
  }

  @Rule(key = "WillRaiseLineIssueOnFileHeaderAndImplementation")
  public static class WillRaiseLineIssueOnFileHeaderAndImplementationCheck extends DelphiCheck {
    @Override
    public DelphiCheckContext visit(FileHeaderNode fileHeader, DelphiCheckContext context) {
      reportIssue(context, fileHeader, MESSAGE);
      return super.visit(fileHeader, context);
    }

    @Override
    public DelphiCheckContext visit(
        ImplementationSectionNode implementationSection, DelphiCheckContext context) {
      reportIssue(context, implementationSection, MESSAGE);
      return super.visit(implementationSection, context);
    }
  }

  @Rule(key = "WillRaiseFileIssue")
  public static class WillRaiseFileIssueCheck extends DelphiCheck {
    @Override
    public DelphiCheckContext visit(DelphiAst ast, DelphiCheckContext context) {
      context.newIssue().withMessage(MESSAGE).report();
      return super.visit(ast, context);
    }
  }
}
