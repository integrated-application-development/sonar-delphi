package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class InlineDeclarationCapturedByAnonymousMethodRuleTest extends BasePmdRuleTest {
  @Test
  void testSimpleInlineVarReferenceShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var Foo: Integer;")
            .appendImpl("  Foo := 123;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("InlineDeclarationCapturedByAnonymousMethodRule"));
  }

  @Test
  void testRegularVarCaptureShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: Integer;")
            .appendImpl("begin")
            .appendImpl("  var Proc: TProc := procedure")
            .appendImpl("    begin")
            .appendImpl("      Foo := 123;")
            .appendImpl("    end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("InlineDeclarationCapturedByAnonymousMethodRule"));
  }

  @Test
  void testInlineVarCaptureShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var Foo: Integer;")
            .appendImpl("  var Proc: TProc := procedure")
            .appendImpl("    begin")
            .appendImpl("      Foo := 123;")
            .appendImpl("    end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(
            1,
            ruleKeyAtLine(
                "InlineDeclarationCapturedByAnonymousMethodRule", builder.getOffset() + 6));
  }

  @Test
  void testRegularVarWithinAnonymousMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var Proc: TProc := procedure")
            .appendImpl("    var")
            .appendImpl("      Foo: Integer;")
            .appendImpl("    begin")
            .appendImpl("      Foo := 123;")
            .appendImpl("    end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("InlineDeclarationCapturedByAnonymousMethodRule"));
  }

  @Test
  void testInlineVarWithinAnonymousMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var Proc: TProc := procedure")
            .appendImpl("    begin")
            .appendImpl("      var Foo: Integer;")
            .appendImpl("      Foo := 123;")
            .appendImpl("    end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("InlineDeclarationCapturedByAnonymousMethodRule"));
  }
}
