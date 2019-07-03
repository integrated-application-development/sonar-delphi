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
package org.sonar.plugins.delphi.pmd;

import net.sourceforge.pmd.RuleViolation;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;

@ScannerSide
public class DelphiPmdViolationRecorder {

  private final DelphiProjectHelper delphiProjectHelper;
  private final ActiveRules activeRules;

  public DelphiPmdViolationRecorder(DelphiProjectHelper projectHelper, ActiveRules activeRules) {
    this.delphiProjectHelper = projectHelper;
    this.activeRules = activeRules;
  }

  public void saveViolation(RuleViolation pmdViolation, SensorContext context) {
    final InputFile inputFile = findResourceFor(pmdViolation);
    if (inputFile == null) {
      // Save violations only for existing resources
      return;
    }

    final RuleKey ruleKey = findActiveRuleKeyFor(pmdViolation);

    if (ruleKey == null) {
      // Save violations only for enabled rules
      return;
    }

    final NewIssue issue = context.newIssue()
        .forRule(ruleKey);

    final TextRange issueTextRange = TextRangeCalculator.calculate(pmdViolation, inputFile);

    final NewIssueLocation issueLocation = issue.newLocation()
        .on(inputFile)
        .message(pmdViolation.getDescription())
        .at(issueTextRange);

    issue.at(issueLocation)
        .save();
  }

  private InputFile findResourceFor(RuleViolation violation) {
    return delphiProjectHelper.getFile(violation.getFilename());
  }

  private RuleKey findActiveRuleKeyFor(RuleViolation violation) {
    final String internalRuleKey = violation.getRule().getName();
    RuleKey ruleKey = RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, internalRuleKey);

    if (activeRules.find(ruleKey) != null) {
      return ruleKey;
    }

    return null;
  }
}
