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

import au.com.integradev.delphi.pmd.FilePosition;
import net.sourceforge.pmd.RuleViolation;
import org.jetbrains.annotations.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * Calculates a {@link org.sonar.api.batch.fs.TextRange} for a given {@link
 * net.sourceforge.pmd.RuleViolation}.
 */
class TextRangeCalculator {
  private static final Logger LOG = Loggers.get(TextRangeCalculator.class);

  private TextRangeCalculator() {
    // Static helper class
  }

  @Nullable
  static TextRange calculate(RuleViolation pmdViolation, InputFile inputFile) {
    TextRange result = null;

    if (pmdViolation.getBeginLine() != FilePosition.UNDEFINED_LINE) {
      if (pmdViolation.getBeginColumn() != FilePosition.UNDEFINED_COLUMN) {
        result = calculateAccurateRange(pmdViolation, inputFile);
      }

      if (result == null) {
        result = calculateLineRange(pmdViolation, inputFile);
      }
    }

    return result;
  }

  private static TextRange calculateAccurateRange(RuleViolation pmdViolation, InputFile inputFile) {
    String ruleKey = pmdViolation.getRule().getName();
    String fileName = pmdViolation.getFilename();
    int beginLine = pmdViolation.getBeginLine();
    int endLine = pmdViolation.getEndLine();
    int beginColumn = pmdViolation.getBeginColumn();
    int endColumn = pmdViolation.getEndColumn();

    try {
      return inputFile.newRange(beginLine, beginColumn, endLine, endColumn);
    } catch (IllegalArgumentException e) {
      String error = "Rule: {} file: {} beginLine: {} beginCol: {} endLine: {} endCol: {}";
      LOG.debug(error, ruleKey, fileName, beginLine, beginColumn, endLine, endColumn);
      LOG.debug("Error while creating issue highlighting text range:", e);
    }

    return null;
  }

  private static TextRange calculateLineRange(RuleViolation pmdViolation, InputFile inputFile) {
    final int startLine = calculateSafeBeginLine(pmdViolation);
    final int endLine = calculateSafeEndLine(pmdViolation);
    final TextPointer startPointer = inputFile.selectLine(startLine).start();
    final TextPointer endPointer = inputFile.selectLine(endLine).end();

    return inputFile.newRange(startPointer, endPointer);
  }

  /**
   * Calculates the endLine of a violation report.
   *
   * @param pmdViolation The violation for which the endLine should be calculated.
   * @return The endLine is assumed to be the line with the biggest number.
   */
  private static int calculateSafeEndLine(RuleViolation pmdViolation) {
    return Math.max(pmdViolation.getBeginLine(), pmdViolation.getEndLine());
  }

  /**
   * Calculates the beginLine of a violation report.
   *
   * @param pmdViolation The violation for which the beginLine should be calculated.
   * @return The beginLine is assumed to be the line with the smallest number. However, if the
   *     smallest number is out-of-range (non-positive), it takes the other number.
   */
  private static int calculateSafeBeginLine(RuleViolation pmdViolation) {
    int minLine = Math.min(pmdViolation.getBeginLine(), pmdViolation.getEndLine());
    return minLine > 0 ? minLine : calculateSafeEndLine(pmdViolation);
  }
}
