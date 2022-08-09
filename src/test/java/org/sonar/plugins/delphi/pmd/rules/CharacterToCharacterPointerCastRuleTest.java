package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class CharacterToCharacterPointerCastRuleTest extends BasePmdRuleTest {
  @Test
  void testCharacterToCharacterPointerShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  AnsiCharacter: AnsiChar;")
            .appendImpl("  Character: Char;")
            .appendImpl("  AnsiCharacterPointer: PAnsiChar;")
            .appendImpl("  CharacterPointer: PChar;")
            .appendImpl("begin")
            .appendImpl("  AnsiCharacterPointer := PAnsiChar(AnsiCharacter);")
            .appendImpl("  AnsiCharacterPointer := PAnsiChar(Character);")
            .appendImpl("  CharacterPointer := PChar(AnsiCharacter);")
            .appendImpl("  CharacterPointer := PChar(Character);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(
            1, ruleKeyAtLine("CharacterToCharacterPointerCastRule", builder.getOffset() + 8))
        .areExactly(
            1, ruleKeyAtLine("CharacterToCharacterPointerCastRule", builder.getOffset() + 9))
        .areExactly(
            1, ruleKeyAtLine("CharacterToCharacterPointerCastRule", builder.getOffset() + 10))
        .areExactly(
            1, ruleKeyAtLine("CharacterToCharacterPointerCastRule", builder.getOffset() + 11));
  }

  @Test
  void testCharacterOrdinalToCharacterPointerShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  AnsiCharacter: AnsiChar;")
            .appendImpl("  Character: Char;")
            .appendImpl("  AnsiCharacterPointer: PAnsiChar;")
            .appendImpl("  CharacterPointer: PChar;")
            .appendImpl("begin")
            .appendImpl("  AnsiCharacterPointer := PAnsiChar(Ord(AnsiCharacter));")
            .appendImpl("  AnsiCharacterPointer := PAnsiChar(Ord(Character));")
            .appendImpl("  CharacterPointer := PChar(Ord(AnsiCharacter));")
            .appendImpl("  CharacterPointer := PChar(Ord(Character));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("CharacterToCharacterPointerCastRule"));
  }

  @Test
  void testCharacterStringToCharacterPointerShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  AnsiCharacter: AnsiChar;")
            .appendImpl("  Character: Char;")
            .appendImpl("  AnsiCharacterPointer: PAnsiChar;")
            .appendImpl("  CharacterPointer: PChar;")
            .appendImpl("begin")
            .appendImpl("  AnsiCharacterPointer := PAnsiChar(AnsiString(AnsiCharacter));")
            .appendImpl("  AnsiCharacterPointer := PAnsiChar(String(Character));")
            .appendImpl("  CharacterPointer := PChar(AnsiString(AnsiCharacter));")
            .appendImpl("  CharacterPointer := PChar(String(Character));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("CharacterToCharacterPointerCastRule"));
  }
}
