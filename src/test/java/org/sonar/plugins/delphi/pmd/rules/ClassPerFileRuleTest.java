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
package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.HasRuleKey.hasRuleKey;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class ClassPerFileRuleTest extends BasePmdRuleTest {

  @Test
  public void testValidRule() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testMoreThanOneClassShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("  end;");
    builder.appendDecl("  TMyClass2 = class");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKey("ClassPerFileRule")));
    assertIssues(hasItem(hasRuleLine(builder.getOffsetDecl() + 4)));
  }

  @Test
  public void testAllClassesAfterFirstOneShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("  end;");
    builder.appendDecl("  TMyClass2 = class");
    builder.appendDecl("  end;");
    builder.appendDecl("  TMyClass3 = class");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(hasSize(2));
    assertIssues(everyItem(hasRuleKey("ClassPerFileRule")));
    assertIssues(hasItem(hasRuleLine(builder.getOffsetDecl() + 4)));
    assertIssues(hasItem(hasRuleLine(builder.getOffsetDecl() + 6)));
  }

  @Test
  public void testFalsePositiveMetaClass() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("  end;");
    builder.appendDecl("  TMetaClassClass = class of TMyClass;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testFalsePositiveClassMethods() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("    class procedure TestProcedure;");
    builder.appendDecl("    class function TestFunction: Boolean;");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(empty());
  }

}
