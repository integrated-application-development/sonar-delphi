/*
 * Copyright (C) 2023 My Organization
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package au.com.integradev.samples.delphi.checks;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class ExtendingTFooCheckTest {
  @Test
  void testNotExtendingFooShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExtendingTFooCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .unitName("Foos")
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  end;")
                .appendDecl("type")
                .appendDecl("  TBar = class")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testExtendingFooShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExtendingTFooCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .unitName("Foos")
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  end;")
                .appendDecl("type")
                .appendDecl("  TBar = class(TFoo) // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }
}
