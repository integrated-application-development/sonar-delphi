package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class IfThenShortCircuitRuleTest extends BasePmdRuleTest {
  private DelphiTestUnitBuilder builder;

  @BeforeEach
  void setup() {
    builder =
        new DelphiTestUnitBuilder()
            .appendDecl("function IfThen(")
            .appendDecl("  Condition: Boolean;")
            .appendDecl("  IfTrue: String;")
            .appendDecl("  IfFalse: String")
            .appendDecl("): String;");
  }

  @Test
  void testNilNotEqualComparisonWithAccessShouldAddIssue() {
    builder
        .appendImpl("function Foo(Bar: TObject): String;")
        .appendImpl("begin")
        .appendImpl("  Result := IfThen(Bar <> nil, Bar.ToString, 'Baz');")
        .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("IfThenShortCircuitRule", builder.getOffset() + 3));
  }

  @Test
  void testNilEqualComparisonWithAccessShouldAddIssue() {
    builder
        .appendImpl("function Foo(Bar: TObject): String;")
        .appendImpl("begin")
        .appendImpl("  Result := IfThen(nil = Bar, 'Baz', Bar.ToString);")
        .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("IfThenShortCircuitRule", builder.getOffset() + 3));
  }

  @Test
  void testAssignedCheckWithAccessShouldAddIssue() {
    builder
        .appendImpl("function Foo(Bar: TObject): String;")
        .appendImpl("begin")
        .appendImpl("  Result := IfThen(Assigned(Bar), Bar.ToString, 'Baz');")
        .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("IfThenShortCircuitRule", builder.getOffset() + 3));
  }

  @Test
  void testNilNotEqualComparisonWithoutAccessShouldNotAddIssue() {
    builder
        .appendImpl("function Foo(Bar: TObject): String;")
        .appendImpl("begin")
        .appendImpl("  Result := IfThen(Bar <> nil, 'Flarp', 'Baz');")
        .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("IfThenShortCircuitRule"));
  }

  @Test
  void testNilEqualComparisonWithoutAccessShouldNotAddIssue() {
    builder
        .appendImpl("function Foo(Bar: TObject): String;")
        .appendImpl("begin")
        .appendImpl("  Result := IfThen(nil = Bar, 'Baz', 'Flarp');")
        .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("IfThenShortCircuitRule"));
  }

  @Test
  void testAssignedCheckWithoutAccessShouldNotAddIssue() {
    builder
        .appendImpl("function Foo(Bar: TObject): String;")
        .appendImpl("begin")
        .appendImpl("  Result := IfThen(Assigned(Bar), 'Flarp', 'Baz');")
        .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("IfThenShortCircuitRule"));
  }

  @Test
  void testIfThenWithWrongNumberOfArgumentsShouldNotAddIssue() {
    builder
        .appendImpl("function Foo(Bar: TObject): String;")
        .appendImpl("begin")
        .appendImpl("  Result := IfThen(Assigned(Bar), Bar.ToString);")
        .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("IfThenShortCircuitRule"));
  }
}
