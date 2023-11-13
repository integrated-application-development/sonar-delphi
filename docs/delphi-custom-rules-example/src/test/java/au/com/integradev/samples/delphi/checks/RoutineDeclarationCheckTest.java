/*
 * Copyright (C) 2023 My Organization
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package au.com.integradev.samples.delphi.checks;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class RoutineDeclarationCheckTest {
  @Test
  void testConstShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RoutineDeclarationCheck())
        .onFile(new DelphiTestUnitBuilder().appendDecl("const Foo = 123;"))
        .verifyNoIssues();
  }

  @Test
  void testProcedureShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RoutineDeclarationCheck())
        .onFile(new DelphiTestUnitBuilder().appendDecl("procedure Foo; // Noncompliant"))
        .verifyIssues();
  }

  @Test
  void testFunctionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RoutineDeclarationCheck())
        .onFile(new DelphiTestUnitBuilder().appendDecl("function Foo; // Noncompliant"))
        .verifyIssues();
  }
}
