package au.com.integradev.delphi.checks;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class InlineAssemblyCheckTest {
  @Test
  void testOrdinaryMethodShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InlineAssemblyCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  WriteLn('Hello world!');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testAsmMethodShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InlineAssemblyCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("asm")
                .appendImpl("  SHR   eax, 16")
                .appendImpl("end;"))
        .verifyIssueOnLine(8);
  }

  @Test
  void testInlineAsmShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InlineAssemblyCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  asm")
                .appendImpl("    SHR   eax, 16")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }
}
