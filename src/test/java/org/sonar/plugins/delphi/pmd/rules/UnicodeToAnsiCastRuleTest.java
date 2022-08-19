package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class UnicodeToAnsiCastRuleTest extends BasePmdRuleTest {
  @Test
  void testWideToNarrowShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  AnsiStr: AnsiString;")
            .appendImpl("  Str: String;")
            .appendImpl("  AnsiCharacter: AnsiChar;")
            .appendImpl("  Character: Char;")
            .appendImpl("begin")
            .appendImpl("  AnsiStr := AnsiString(Str);")
            .appendImpl("  AnsiCharacter := AnsiChar(Character);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("UnicodeToAnsiCastRule", builder.getOffset() + 8))
        .areExactly(1, ruleKeyAtLine("UnicodeToAnsiCastRule", builder.getOffset() + 9));
  }

  @Test
  void testNarrowToWideShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  AnsiStr: AnsiString;")
            .appendImpl("  Str: String;")
            .appendImpl("  AnsiCharacter: AnsiChar;")
            .appendImpl("  Character: Char;")
            .appendImpl("begin")
            .appendImpl("  AnsiStr := String(AnsiStr);")
            .appendImpl("  AnsiCharacter := Char(AnsiCharacter);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnicodeToAnsiCastRule"));
  }
}
