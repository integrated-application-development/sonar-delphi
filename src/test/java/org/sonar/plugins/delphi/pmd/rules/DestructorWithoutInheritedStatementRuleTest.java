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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class DestructorWithoutInheritedStatementRuleTest extends BasePmdRuleTest {

  @Test
  public void testValidRule() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("type");
    builder.appendDecl("  TTestDestructor = class");
    builder.appendDecl("  public");
    builder.appendDecl("    destructor Destroy; override;");
    builder.appendDecl("  end;");

    builder.appendImpl("destructor TTestConstructor.Destroy;");
    builder.appendImpl("begin");
    builder.appendImpl("  inherited;");
    builder.appendImpl("  Writeln('do something');");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testDestructorMissingInheritedShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("type");
    builder.appendDecl("  TTestDestructor = class");
    builder.appendDecl("  public");
    builder.appendDecl("    destructor Destroy; override;");
    builder.appendDecl("  end;");

    builder.appendImpl("destructor TTestConstructor.Destroy;");
    builder.appendImpl("begin");
    builder.appendImpl("  Writeln('do something');");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("DestructorWithoutInheritedStatementRule", builder.getOffSet() + 1)));
  }

}
