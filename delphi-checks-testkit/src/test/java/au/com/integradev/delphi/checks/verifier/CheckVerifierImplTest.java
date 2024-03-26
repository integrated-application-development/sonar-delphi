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
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.FileHeaderNode;
import org.sonar.plugins.communitydelphi.api.ast.ImplementationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;

class CheckVerifierImplTest {
  private static final String MESSAGE = "Test message";

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
  void testImpliedQuickFixWithPositiveOffset() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("const")
                    .appendDecl("  Foo = 0;")
                    .appendDecl("// Fix@[+1:8 to +1:11] <<BarBaz>>")
                    .appendDecl("  Bar = Foo; // Noncompliant"));

    assertThatCode(verifier::verifyIssues).doesNotThrowAnyException();
  }

  @Test
  void testQuickFixCommentWithNoReplacementText() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("const")
                    .appendDecl("  Foo = 0;")
                    .appendDecl("// Fix@[+1:8 to +1:11]")
                    .appendDecl("  Bar = Foo; // Noncompliant"));

    assertThatThrownBy(verifier::verifyIssues).isInstanceOf(AssertionError.class);
  }

  @Test
  void testImpliedQuickFixWithNegativeOffset() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("const")
                    .appendDecl("  Foo = 0;")
                    .appendDecl("  Bar = Foo; // Noncompliant")
                    .appendDecl("// Fix@[-1:8 to -1:11] <<BarBaz>>"));

    assertThatCode(verifier::verifyIssues).doesNotThrowAnyException();
  }

  @Test
  void testImpliedQuickFixWithMultipleEdits() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooAndArgsCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("function Foo(const Blomp: Integer): Integer;")
                    .appendDecl("var")
                    .appendDecl("  GBar: Integer = Foo(42); // Noncompliant")
                    .appendDecl("// Fix qf1@[-1:18 to -1:21] <<BarBaz>>")
                    .appendDecl("// Fix qf1@[-2:21 to -2:25] <<(Flarp)>>"));

    assertThatCode(verifier::verifyIssues).doesNotThrowAnyException();
  }

  @Test
  void testMultipleQuickFixes() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooAndArgsCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("function Foo(const Blomp: Integer): Integer;")
                    .appendDecl("var")
                    .appendDecl("  GBar: Integer = Foo(42); // Noncompliant")
                    .appendDecl("// Fix qf1@[-1:18 to -1:21] <<BarBaz>>")
                    .appendDecl("// Fix qf1@[-2:21 to -2:25] <<(Flarp)>>")
                    .appendImpl("initialization")
                    .appendImpl("  // Fix qf2@[+2:2 to +2:5] <<BarBaz>>")
                    .appendImpl("  // Fix qf2@[+1:5 to +1:9] <<(Flarp)>>")
                    .appendImpl("  Foo(84); // Noncompliant"));

    assertThatCode(verifier::verifyIssues).doesNotThrowAnyException();
  }

  @Test
  void testMultipleQuickFixesWithUnnamedFix() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooAndArgsCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("function Foo(const Blomp: Integer): Integer;")
                    .appendDecl("var")
                    .appendDecl("  GBar: Integer = Foo(42); // Noncompliant")
                    .appendDecl("// Fix@[-1:18 to -1:21] <<BarBaz>>")
                    .appendDecl("// Fix@[-2:21 to -2:25] <<(Flarp)>>")
                    .appendImpl("initialization")
                    .appendImpl("  // Fix qf2@[+2:2 to +2:5] <<BarBaz>>")
                    .appendImpl("  // Fix qf2@[+1:5 to +1:9] <<(Flarp)>>")
                    .appendImpl("  Foo(84); // Noncompliant"));

    assertThatCode(verifier::verifyIssues).doesNotThrowAnyException();
  }

  @Test
  void testMultipleUnnamedQuickFixes() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooAndArgsCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("function Foo(const Blomp: Integer): Integer;")
                    .appendDecl("var")
                    .appendDecl("  GBar: Integer = Foo(42); // Noncompliant")
                    .appendDecl("// Fix@[-1:18 to -1:21] <<BarBaz>>")
                    .appendDecl("// Fix@[-2:21 to -2:25] <<(Flarp)>>")
                    .appendImpl("initialization")
                    .appendImpl("  // Fix@[+2:2 to +2:5] <<BarBaz>>")
                    .appendImpl("  // Fix@[+1:5 to +1:9] <<(Flarp)>>")
                    .appendImpl("  Foo(84); // Noncompliant"));

    assertThatThrownBy(verifier::verifyIssues).isInstanceOf(AssertionError.class);
  }

  @Test
  void testQuickFixesWhenNoQuickFixesAsserted() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooAndArgsCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("function Foo(const Blomp: Integer): Integer;")
                    .appendDecl("var")
                    .appendDecl("  GBar: Integer = Foo(42); // Noncompliant"));

    assertThatCode(verifier::verifyIssues).doesNotThrowAnyException();
  }

  @Test
  void testMatchingEditsAcrossMismatchingFixes() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooAndArgsCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("function Foo(const Blomp: Integer): Integer;")
                    .appendDecl("var")
                    .appendDecl("  GBar: Integer = Foo(42); // Noncompliant")
                    .appendDecl("// Fix qf1@[-1:18 to -1:21] <<BarBaz>>")
                    .appendDecl("// Fix qf2@[-2:21 to -2:25] <<(Flarp)>>"));

    assertThatThrownBy(verifier::verifyIssues).isInstanceOf(AssertionError.class);
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

  @Rule(key = "WillRaiseQuickFixOnFoo")
  public static class WillRaiseQuickFixOnFooCheck extends DelphiCheck {
    @Override
    public DelphiCheckContext visit(
        NameReferenceNode nameReferenceNode, DelphiCheckContext context) {
      if (nameReferenceNode.getImage().equalsIgnoreCase("Foo")) {
        context
            .newIssue()
            .onNode(nameReferenceNode)
            .withMessage(MESSAGE)
            .withQuickFixes(
                QuickFix.newFix(MESSAGE)
                    .withEdit(QuickFixEdit.replace(nameReferenceNode, "BarBaz")))
            .report();
      }
      return super.visit(nameReferenceNode, context);
    }
  }

  @Rule(key = "WillRaiseQuickFixOnFooAndArgs")
  public static class WillRaiseQuickFixOnFooAndArgsCheck extends DelphiCheck {
    @Override
    public DelphiCheckContext visit(
        NameReferenceNode nameReferenceNode, DelphiCheckContext context) {
      DelphiNode argList =
          nameReferenceNode.getParent().getChild(nameReferenceNode.getChildIndex() + 1);

      if (argList instanceof ArgumentListNode
          && nameReferenceNode.getImage().equalsIgnoreCase("Foo")) {
        context
            .newIssue()
            .onNode(nameReferenceNode)
            .withMessage(MESSAGE)
            .withQuickFixes(
                QuickFix.newFix(MESSAGE)
                    .withEdits(
                        QuickFixEdit.replace(nameReferenceNode, "BarBaz"),
                        QuickFixEdit.replace(argList, "(Flarp)")))
            .report();
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
