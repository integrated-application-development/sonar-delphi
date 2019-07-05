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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.sonar.plugins.delphi.HasRuleKey.hasRuleKey;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleLine;

import org.junit.Test;

public class ClassPerFileRuleTest extends BasePmdRuleTest {

  @Test
  public void testValidRule() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("  end;");

    execute(builder);

    assertThat(stringifyIssues(), issues, is(empty()));
  }

  @Test
  public void moreThanOneClassShouldAddIssue() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("  end;");
    builder.appendDecl("  TMyClass2 = class");
    builder.appendDecl("  end;");

    execute(builder);

    assertThat(issues, hasSize(1));
    assertThat(issues, hasItem(hasRuleKey("ClassPerFileRule")));
    assertThat(issues, hasItem(hasRuleLine(builder.getOffsetDecl() + 4)));
  }

  @Test
  public void testAllClassesAfterFirstOneShouldAddIssue() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("  end;");
    builder.appendDecl("  TMyClass2 = class");
    builder.appendDecl("  end;");
    builder.appendDecl("  TMyClass3 = class");
    builder.appendDecl("  end;");

    execute(builder);

    assertThat(issues, hasSize(2));
    assertThat(issues, everyItem(hasRuleKey("ClassPerFileRule")));
    assertThat(issues, hasItem(hasRuleLine(builder.getOffsetDecl() + 4)));
    assertThat(issues, hasItem(hasRuleLine(builder.getOffsetDecl() + 6)));
  }

  @Test
  public void testFalsePositiveMetaClass() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("  end;");
    builder.appendDecl("  TMetaClassClass = class of TMyClass;");

    execute(builder);

    assertThat(stringifyIssues(), issues, is(empty()));
  }

  @Test
  public void testFalsePositiveClassMethods() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("    class procedure TestProcedure;");
    builder.appendDecl("    class function TestFunction: Boolean;");
    builder.appendDecl("  end;");

    execute(builder);

    assertThat(stringifyIssues(), issues, is(empty()));
  }

}
