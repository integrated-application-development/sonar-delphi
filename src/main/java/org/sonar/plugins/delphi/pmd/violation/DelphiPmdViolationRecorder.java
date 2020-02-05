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
package org.sonar.plugins.delphi.pmd.violation;

import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.SCOPE;

import net.sourceforge.pmd.RuleViolation;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.RuleScope;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.project.DelphiProjectHelper;

@ScannerSide
public class DelphiPmdViolationRecorder {
  private final DelphiProjectHelper delphiProjectHelper;
  private final ActiveRules activeRules;

  public DelphiPmdViolationRecorder(DelphiProjectHelper projectHelper, ActiveRules activeRules) {
    this.delphiProjectHelper = projectHelper;
    this.activeRules = activeRules;
  }

  public void saveViolation(RuleViolation pmdViolation, SensorContext context) {
    if (pmdViolation.isSuppressed()) {
      // Suppressed violations shouldn't be saved
      return;
    }

    if (isOutOfScope(pmdViolation)) {
      // Save violations only if they occur within the rule's specified scope (ALL/MAIN/TEST)
      return;
    }

    final ActiveRule activeRule = findActiveRuleFor(pmdViolation);

    if (activeRule == null) {
      // Save violations only for enabled rules
      return;
    }

    final NewIssue issue = context.newIssue().forRule(activeRule.ruleKey());
    final InputFile inputFile = findResourceFor(pmdViolation);
    final NewIssueLocation issueLocation =
        issue.newLocation().on(inputFile).message(pmdViolation.getDescription());

    TextRange textRange = TextRangeCalculator.calculate(pmdViolation, inputFile);
    if (textRange != null) {
      issueLocation.at(textRange);
    }

    issue.at(issueLocation);

    issue.save();
  }

  private InputFile findResourceFor(RuleViolation violation) {
    InputFile inputFile = delphiProjectHelper.getFile(violation.getFilename());
    if (inputFile == null) {
      throw new RuntimeException(
          String.format("Input file could not be found: '%s'", violation.getFilename()));
    }
    return inputFile;
  }

  private ActiveRule findActiveRuleFor(RuleViolation violation) {
    final String internalRuleKey = violation.getRule().getName();
    RuleKey ruleKey = RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, internalRuleKey);

    return activeRules.find(ruleKey);
  }

  private boolean isOutOfScope(RuleViolation violation) {
    String scopeProperty = violation.getRule().getProperty(SCOPE);
    RuleScope scope = RuleScope.valueOf(scopeProperty);

    switch (scope) {
      case MAIN:
        return isInsideTestMethod(violation);
      case TEST:
        return !isInsideTestMethod(violation);
      default:
        return false;
    }
  }

  private boolean isInsideTestMethod(RuleViolation violation) {
    return violation.getClassName().matches(delphiProjectHelper.testTypeRegex());
  }
}
