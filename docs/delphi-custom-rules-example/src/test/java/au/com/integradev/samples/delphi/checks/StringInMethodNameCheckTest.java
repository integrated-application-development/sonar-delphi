/*
 * Copyright (C) 2023 My Organization
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package au.com.integradev.samples.delphi.checks;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

class StringInMethodNameCheckTest {
  @Test
  void testNameWithoutStringShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(new DelphiTestUnitBuilder().appendDecl("procedure MethodWithBar;"))
        .verifyNoIssues();
  }

  @Test
  void testNameWithStringShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(new DelphiTestUnitBuilder().appendDecl("procedure MethodWithFoo; // Noncompliant"))
        .verifyIssues();
  }

  @Test
  void testNameWithUppercaseBrandShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(new DelphiTestUnitBuilder().appendDecl("procedure MethodWithFOO; // Noncompliant"))
        .verifyIssues();
  }

  private static DelphiCheck createCheck() {
    StringInMethodNameCheck check = new StringInMethodNameCheck();
    check.string = "Foo";
    return check;
  }
}
