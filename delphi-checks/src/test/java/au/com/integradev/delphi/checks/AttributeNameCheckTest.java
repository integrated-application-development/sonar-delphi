package au.com.integradev.delphi.checks;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

class AttributeNameCheckTest {
  private static DelphiCheck createCheck(String setting) {
    AttributeNameCheck check = new AttributeNameCheck();
    check.attributeSuffix = setting;
    return check;
  }

  @ParameterizedTest
  @ValueSource(strings = {"allowed", "required"})
  void testAllowedOrRequiredAttributeTypeWithSuffixShouldNotAddIssue(String setting) {
    CheckVerifier.newVerifier()
        .withCheck(createCheck(setting))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  FooAttribute = class(TCustomAttribute)")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {"allowed", "forbidden"})
  void testAllowedOrForbiddenAttributeTypeWithoutSuffixShouldNotAddIssue(String setting) {
    CheckVerifier.newVerifier()
        .withCheck(createCheck(setting))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  Foo = class(TCustomAttribute)")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testRequiredAttributeTypeWithoutSuffixShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck("required"))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  Foo = class(TCustomAttribute)")
                .appendDecl("  end;"))
        .verifyIssueOnLine(6);
  }

  @Test
  void testForbiddenAttributeTypeWithSuffixShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck("forbidden"))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  FooAttribute = class(TCustomAttribute)")
                .appendDecl("  end;"))
        .verifyIssueOnLine(6);
  }

  @ParameterizedTest
  @ValueSource(strings = {"allowed", "required", "forbidden"})
  void testNonPascalCaseAttributeTypeShouldAddIssue(String setting) {
    CheckVerifier.newVerifier()
        .withCheck(createCheck(setting))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  fooAttribute = class(TCustomAttribute)")
                .appendDecl("  end;"))
        .verifyIssueOnLine(6);
  }
}
