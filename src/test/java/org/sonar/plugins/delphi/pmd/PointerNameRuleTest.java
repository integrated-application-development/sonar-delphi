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
import org.sonar.api.issue.Issue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PointerNameRuleTest extends BasePmdRuleTest {

  @Test
  public void testValidRule() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  PMyPointer = ^Integer;");

    analyse(builder);

    assertThat(issues, is(empty()));
  }

  @Test
  public void testInvalidRule() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  pMyPointer = ^Integer;");

    analyse(builder);

    assertThat(issues, hasSize(1));
    Issue issue = issues.get(0);
    assertThat(issue.ruleKey().rule(), equalTo("PointerNameRule"));
    assertThat(issue.line(), is(builder.getOffsetDecl() + 2));
  }

  @Test
  public void testAvoidFalsePositive() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  Pointer = ^Integer;");

    analyse(builder);

    assertThat(issues, hasSize(1));
    Issue issue = issues.get(0);
    assertThat(issue.ruleKey().rule(), equalTo("PointerNameRule"));
    assertThat(issue.line(), is(builder.getOffsetDecl() + 2));
  }

  @Test
  public void shouldIgnorePointerAssignment() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendImpl("procedure Foo;");
    builder.appendImpl("var");
    builder.appendImpl("  MyInteger: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  MyInteger := PInteger(1)^;");
    builder.appendImpl("end;");

    analyse(builder);

    assertThat(issues, is(empty()));
  }
}
