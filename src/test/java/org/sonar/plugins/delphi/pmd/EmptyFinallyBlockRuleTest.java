/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colombo
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

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKey;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleLine;

public class EmptyFinallyBlockRuleTest extends BasePmdRuleTest {

  @Test
  public void validRule() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendImpl("procedure Test();");
    builder.appendImpl("begin");
    builder.appendImpl("  try");
    builder.appendImpl("    writeln('try block');");
    builder.appendImpl("  finally");
    builder.appendImpl("    writeln('finally block');");
    builder.appendImpl("  end;");
    builder.appendImpl("end;");

    analyse(builder);

    assertThat(issues, is(empty()));
  }

  @Test
  public void testEmptyFinallyBlock() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendImpl("procedure Test();");
    builder.appendImpl("begin");
    builder.appendImpl("  try");
    builder.appendImpl("    writeln('try block');");
    builder.appendImpl("  finally");
    builder.appendImpl("  end;");
    builder.appendImpl("end;");

    analyse(builder);

    assertThat(issues.toString(), issues, hasSize(1));
    assertThat(issues, hasItem(allOf(hasRuleKey("EmptyFinallyBlockRule"), hasRuleLine(builder.getOffSet() + 5))));
  }

}
