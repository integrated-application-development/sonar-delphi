package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class InheritedMethodWithNoCodeRuleTest extends BasePmdRuleTest {

  @Test
  void testShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  inherited;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffset() + 3));
  }

  @Test
  void testNoSemicolonShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  inherited")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffset() + 3));
  }

  @Test
  void testImplementationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  inherited;")
            .appendImpl("  FMyField := 5;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testImplementationWithInheritedAtEndShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  FMyField := 5;")
            .appendImpl("  inherited;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testFalsePositiveImplementationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  FMyField := 5;")
            .appendImpl("  if MyBoolean then begin")
            .appendImpl("    inherited;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testExplicitInheritedShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  inherited MyProcedure;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffset() + 3));
  }

  @Test
  void testExplicitInheritedWithArgumentsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure(X, Y);")
            .appendImpl("begin")
            .appendImpl("  inherited MyProcedure(X, Y);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffset() + 3));
  }

  @Test
  void testExplicitInheritedWithMismatchedArgumentSizesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure(X, Y);")
            .appendImpl("begin")
            .appendImpl("  inherited MyProcedure(X);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffset() + 3));
  }

  @Test
  void testExplicitInheritedWithMismatchedArgumentsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure(X, Y);")
            .appendImpl("begin")
            .appendImpl("  inherited MyProcedure(True, False);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffset() + 3));
  }

  @Test
  void testExplicitInheritedWithEmptyBracketsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  inherited MyProcedure();")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffset() + 3));
  }

  @Test
  void testWrongExplicitInheritedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  inherited SomeOtherProcedure;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testFunctionInheritedShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function MyFunction: Integer;")
            .appendImpl("begin")
            .appendImpl("  Result := inherited MyFunction;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffset() + 3));
  }

  @Test
  void testFunctionQualifiedInheritedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function MyFunction: Integer;")
            .appendImpl("begin")
            .appendImpl("  Result := inherited MyFunction[0].GetValue;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
