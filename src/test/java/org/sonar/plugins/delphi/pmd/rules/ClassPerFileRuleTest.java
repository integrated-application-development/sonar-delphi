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

import static org.sonar.plugins.delphi.pmd.FilePosition.UNDEFINED_LINE;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class ClassPerFileRuleTest extends BasePmdRuleTest {

  @Test
  public void testOneClassShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class(TObject)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testForwardTypesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class;");
    builder.appendDecl("  TMyClass = class(TObject)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testTwoClassesShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class(TObject)");
    builder.appendDecl("  end;");
    builder.appendDecl("  TMyClass2 = class(TObject)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().hasSize(1).areExactly(1, ruleKeyAtLine("ClassPerFileRule", UNDEFINED_LINE));
  }

  @Test
  public void testMultipleViolationsShouldAddOneIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class(TObject)");
    builder.appendDecl("  end;");
    builder.appendDecl("  TMyClass2 = class(TObject)");
    builder.appendDecl("  end;");
    builder.appendDecl("  TMyClass3 = class(TObject)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().hasSize(1).areExactly(1, ruleKeyAtLine("ClassPerFileRule", UNDEFINED_LINE));
  }

  @Test
  public void testFalsePositiveMetaClass() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class(TObject)");
    builder.appendDecl("  end;");
    builder.appendDecl("  TMetaClassClass = class of TMyClass;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testFalsePositiveClassMethods() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class(TObject)");
    builder.appendDecl("    class procedure TestProcedure;");
    builder.appendDecl("    class function TestFunction: Boolean;");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
