/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
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
