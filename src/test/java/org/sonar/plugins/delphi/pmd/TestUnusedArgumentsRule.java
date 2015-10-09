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

import java.util.Arrays;
import org.junit.Test;
import org.sonar.api.issue.Issue;
import org.sonar.plugins.delphi.debug.DebugSensorContext;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TestUnusedArgumentsRule extends BasePmdRuleTest {

  @Test
  public void testRule() {
    configureTest(ROOT_DIR_NAME + "/UnusedARgumentRule.pas");

    DebugSensorContext debugContext = new DebugSensorContext();
    sensor.analyse(project, debugContext);

    // all expected rule violations and their lines
    RuleData ruleData[] = {
      new RuleData("UnusedArgumentsRule", 31)
    };

    // Sort the violations by line number, so we don't have to add
    // violations order
    Arrays.sort(ruleData, RuleData.getComparator());

    assertThat("number of issues", issues, hasSize(1));

    for (int i = 0; i < issues.size(); ++i) {
      Issue issue = issues.get(i);

      System.out.println(issue.ruleKey().rule() + ":" + issue.line());

      assertThat("rule " + ruleData[i].toString(), ruleData[i].getName(), is(issue.ruleKey().rule()));
      assertThat("rule " + ruleData[i].toString() + "line ", ruleData[i].getLine(), is(issue.line()));
    }
  }
}
