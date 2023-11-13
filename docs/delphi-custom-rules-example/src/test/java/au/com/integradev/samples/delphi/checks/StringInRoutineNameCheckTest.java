/*
 * Copyright (C) 2023 My Organization
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package au.com.integradev.samples.delphi.checks;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

class StringInRoutineNameCheckTest {
  @Test
  void testNameWithoutStringShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(new DelphiTestUnitBuilder().appendDecl("procedure RoutineWithBar;"))
        .verifyNoIssues();
  }

  @Test
  void testNameWithStringShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(new DelphiTestUnitBuilder().appendDecl("procedure RoutineWithFoo; // Noncompliant"))
        .verifyIssues();
  }

  @Test
  void testNameWithUppercaseBrandShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(new DelphiTestUnitBuilder().appendDecl("procedure RoutineWithFOO; // Noncompliant"))
        .verifyIssues();
  }

  private static DelphiCheck createCheck() {
    StringInRoutineNameCheck check = new StringInRoutineNameCheck();
    check.string = "Foo";
    return check;
  }
}
