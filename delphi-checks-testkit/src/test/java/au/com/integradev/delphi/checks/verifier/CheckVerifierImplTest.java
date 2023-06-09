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
