/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
package org.sonar.plugins.delphi.pmd;

import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.issue.Issuable.IssueBuilder;
import org.sonar.api.issue.Issue;
import org.sonar.api.rule.RuleKey;

import javax.annotation.Nullable;

public class StubIssueBuilder implements IssueBuilder {

  private RuleKey ruleKey;
  private Integer line;
  private String message;

  @Override
  public IssueBuilder ruleKey(RuleKey ruleKey) {
    this.ruleKey = ruleKey;
    return this;
  }

  @Override
  public IssueBuilder line(@Nullable Integer integer) {
    return null;
  }

  @Override
  public IssueBuilder message(@Nullable String s) {
    return null;
  }

  @Override
  public NewIssueLocation newLocation() {
    return null;
  }

  @Override
  public IssueBuilder at(NewIssueLocation newIssueLocation) {
    return null;
  }

  @Override
  public IssueBuilder addLocation(NewIssueLocation newIssueLocation) {
    return null;
  }

  @Override
  public IssueBuilder addFlow(Iterable<NewIssueLocation> iterable) {
    return null;
  }

  @Override
  public IssueBuilder severity(String severity) {
    return this;
  }

  @Override
  public IssueBuilder reporter(String reporter) {
    return this;
  }

  @Override
  public IssueBuilder effortToFix(Double d) {
    return this;
  }

  @Override
  public IssueBuilder attribute(String key, String value) {
    return this;
  }

  @Override
  public Issue build() {
    //DefaultIssue defaultIssue = new DefaultIssue().forRule(ruleKey);
    return new DelphiIssue(line, ruleKey, message)
            .getIssue();
    }//(Issue)defaultIssue;
}
