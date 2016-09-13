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

public class ClassNameRuleTest extends BasePmdRuleTest {

  @Test
  public void testValidRule() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("  end;");

    analyse(builder);

    assertThat(issues, is(empty()));
  }

  @Test
  public void classNameWithoutPrefixShouldAddIssue() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  MyClass = class");
    builder.appendDecl("  end;");

    analyse(builder);

    assertThat(issues, hasSize(1));
    Issue issue = issues.get(0);
    assertThat(issue.ruleKey().rule(), equalTo("ClassNameRule"));
    assertThat(issue.line(), is(builder.getOffsetDecl() + 2));
  }

  @Test
  public void classNameDoNotStartsWithCapitalLetterShouldAddIssue() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TmyClass = class");
    builder.appendDecl("  end;");

    analyse(builder);

    assertThat(issues, hasSize(1));
    Issue issue = issues.get(0);
    System.out.println("HERE10:  RULEKEY:"+issues.get(0).ruleKey().rule());
    System.out.println("HERE10:  LINE:"+issues.get(0).line());
    System.out.println("HERE10:  CLASS:"+issues.get(0).getClass());
    assertThat(issue.ruleKey().rule(), equalTo("ClassNameRule"));
    assertThat(issue.line(), is(builder.getOffsetDecl() + 2));
  }

  @Test
  public void testAvoidFalsePositive() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TestClass = class");
    builder.appendDecl("  end;");

    analyse(builder);

    assertThat(issues, hasSize(1));
    Issue issue = issues.get(0);
    assertThat(issue.ruleKey().rule(), equalTo("ClassNameRule"));
    assertThat(issue.line(), is(builder.getOffsetDecl() + 2));
  }

  @Test
  public void testNestedType() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TOuterClass = class");
    builder.appendDecl("  strict private");
    builder.appendDecl("    type");
    builder.appendDecl("      TInnerClass1 = class");
    builder.appendDecl("      end;");
    builder.appendDecl("      TInnerClass2 = class");
    builder.appendDecl("      end;");
    builder.appendDecl("  end;");

    analyse(builder);

    assertThat(toString(issues), issues, empty());
  }

  @Test
  public void acceptCapitalLetterEforExceptionClasses() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  EMyCustomException = class(Exception)");
    builder.appendDecl("  end;");

    analyse(builder);

    assertThat(issues, is(empty()));
  }
}
