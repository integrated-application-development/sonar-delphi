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
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
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
  void testImpliedIssueOnExactMatchingLines() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseIssueOnFooCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("const")
                    .appendDecl("  Foo = 0;")
                    .appendDecl("  Foo2 = Foo; // Noncompliant")
                    .appendImpl("const")
                    .appendImpl("  Bar = Foo; // Noncompliant"));

    assertThatCode(verifier::verifyIssues).doesNotThrowAnyException();

    assertThatThrownBy(verifier::verifyIssueOnFile).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyIssueOnProject).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyNoIssues).isInstanceOf(AssertionError.class);
  }

  @Test
  void testImpliedIssuesWithOffset() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseIssueOnFooCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("const")
                    .appendDecl("  Foo = 0;")
                    .appendDecl("  // Noncompliant@+1")
                    .appendDecl("  Foo2 = Foo;")
                    .appendImpl("const")
                    .appendImpl("  Bar = Foo;")
                    .appendImpl("  Bar2 = Foo;")
                    .appendImpl("  // Noncompliant@-1")
                    .appendImpl("  // Noncompliant@-3"));

    assertThatCode(verifier::verifyIssues).doesNotThrowAnyException();

    assertThatThrownBy(verifier::verifyIssueOnFile).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyIssueOnProject).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyNoIssues).isInstanceOf(AssertionError.class);
  }

  @Test
  void testImpliedIssueOnNonMatchingLines() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseIssueOnFooCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("const")
                    .appendDecl("  Baz = 0; // Noncompliant")
                    .appendImpl("const")
                    .appendImpl("  // Noncompliant@+1")
                    .appendImpl("  Bar = Baz;"));

    assertThatCode(verifier::verifyNoIssues).doesNotThrowAnyException();

    assertThatThrownBy(verifier::verifyIssues).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyIssueOnFile).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyIssueOnProject).isInstanceOf(AssertionError.class);
  }

  @Test
  void testFileIssue() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseFileIssueCheck())
            .onFile(new DelphiTestUnitBuilder());

    assertThatCode(verifier::verifyIssueOnFile).doesNotThrowAnyException();

    assertThatThrownBy(verifier::verifyIssues).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyIssueOnProject).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyNoIssues).isInstanceOf(AssertionError.class);
  }

  @Rule(key = "WillRaiseIssueOnFoo")
  public static class WillRaiseIssueOnFooCheck extends DelphiCheck {
    @Override
    public DelphiCheckContext visit(
        NameReferenceNode nameReferenceNode, DelphiCheckContext context) {
      if (nameReferenceNode.getImage().equalsIgnoreCase("Foo")) {
        reportIssue(context, nameReferenceNode, MESSAGE);
      }
      return super.visit(nameReferenceNode, context);
    }
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
