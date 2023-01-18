/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package au.com.integradev.delphi.pmd.violation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleViolation;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;

class TextRangeCalculatorTest {

  private final InputFile testInput = createTestInput();

  @Test
  void testCalculateTextRange() {
    final RuleViolation violation = createRuleViolation(1, 1, 3, 1);
    final TextRange range = TextRangeCalculator.calculate(violation, testInput);

    assertTextRangeValues(range, 1, 1, 3, 1);
  }

  @Test
  void testWhenEndLineIsGreaterThanBeginLineThenLineNumbersAreFlipped() {
    final RuleViolation violation = createRuleViolation(3, 1, 1, 1);
    final TextRange range = TextRangeCalculator.calculate(violation, testInput);

    assertTextRangeLines(range, 1, 3);
  }

  @Test
  void testWhenEndLineEqualsBeginLineThenRangeStartAndEndsAtSameLine() {
    final RuleViolation violation = createRuleViolation(2, 1, 2, 2);
    final TextRange range = TextRangeCalculator.calculate(violation, testInput);

    assertTextRangeLines(range, 2, 2);
  }

  @Test
  void testWhenBeginLineIsNegativeThenRangeStartsAndEndsAtEndLine() {
    final RuleViolation violation = createRuleViolation(-1, 1, 1, 2);
    final TextRange range = TextRangeCalculator.calculate(violation, testInput);

    assertTextRangeLines(range, 1, 1);
  }

  @Test
  void testWhenEndLineIsNegativeThenRangeStartsAndEndsAtBeginLine() {
    final RuleViolation violation = createRuleViolation(1, 1, -1, 2);
    final TextRange range = TextRangeCalculator.calculate(violation, testInput);

    assertTextRangeLines(range, 1, 1);
  }

  @Test
  void testWhenColumnValuesAreBadThenFullLinesAreSelected() {
    final RuleViolation violation = createRuleViolation(2, -1, 2, -1);
    final TextRange range = TextRangeCalculator.calculate(violation, testInput);

    assertTextRangeValues(range, 2, 0, 2, 5);
  }

  private void assertTextRangeValues(
      TextRange range, int beginLine, int beginCol, int endLine, int endCol) {
    assertThat(range).isNotNull();

    assertThat(range.start()).isNotNull();
    assertThat(range.start().line()).isEqualTo(beginLine);
    assertThat(range.start().lineOffset()).isEqualTo(beginCol);

    assertThat(range.end()).isNotNull();
    assertThat(range.end().line()).isEqualTo(endLine);
    assertThat(range.end().lineOffset()).isEqualTo(endCol);
  }

  private void assertTextRangeLines(TextRange range, int beginLine, int endLine) {
    assertThat(range).isNotNull();

    assertThat(range.start()).isNotNull();
    assertThat(range.start().line()).isEqualTo(beginLine);

    assertThat(range.end()).isNotNull();
    assertThat(range.end().line()).isEqualTo(endLine);
  }

  private RuleViolation createRuleViolation(int beginLine, int beginCol, int endLine, int endCol) {
    RuleViolation mockedViolation = mock(RuleViolation.class);
    Rule mockedRule = mock(Rule.class);

    when(mockedViolation.getBeginLine()).thenReturn(beginLine);
    when(mockedViolation.getBeginColumn()).thenReturn(beginCol);
    when(mockedViolation.getEndLine()).thenReturn(endLine);
    when(mockedViolation.getEndColumn()).thenReturn(endCol);
    when(mockedViolation.getRule()).thenReturn(mockedRule);
    when(mockedRule.getName()).thenReturn("TEST");

    return mockedViolation;
  }

  private InputFile createTestInput() {
    return TestInputFileBuilder.create("moduleKey", "relPath")
        .setContents("This\n" + "is a \n" + "multi-line file. \n")
        .build();
  }
}
