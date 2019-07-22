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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class EmptyBeginStatementTest extends BasePmdRuleTest {
  @Test
  public void testValidRule() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("type");
    builder.appendDecl("  TEmptyProcs = class");
    builder.appendDecl("  public");
    builder.appendDecl("    procedure One;");
    builder.appendDecl("    procedure Two;");
    builder.appendDecl("    procedure Three;");
    builder.appendDecl("  end;");

    builder.appendImpl("procedure TEmptyProcs.One;");
    builder.appendImpl("begin");
    builder.appendImpl("  Writeln('OK');");
    builder.appendImpl("end;");
    builder.appendImpl("procedure TEmptyProcs.Two;");
    builder.appendImpl("begin");
    builder.appendImpl("  Writeln('OK');");
    builder.appendImpl("end;");
    builder.appendImpl("procedure TEmptyProcs.Three;");
    builder.appendImpl("begin");
    builder.appendImpl("  Writeln('OK');");
    builder.appendImpl("end;");
    builder.appendImpl("procedure GlobalProcedureFour;");
    builder.appendImpl("begin");
    builder.appendImpl("  Writeln('OK');");
    builder.appendImpl("end;");
    builder.appendImpl("procedure GlobalProcedureFive;");
    builder.appendImpl("begin");
    builder.appendImpl("  Writeln('OK');");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testEmptyBeginStatements() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("type");
    builder.appendDecl("  TEmptyProcs = class");
    builder.appendDecl("  public");
    builder.appendDecl("    procedure One;");
    builder.appendDecl("    procedure Two;");
    builder.appendDecl("    procedure Three;");
    builder.appendDecl("  end;");

    builder.appendImpl("procedure TEmptyProcs.One;");
    builder.appendImpl("begin");
    builder.appendImpl("  Writeln('OK');");
    builder.appendImpl("end;");
    builder.appendImpl("procedure TEmptyProcs.Two;");
    builder.appendImpl("begin");
    builder.appendImpl("  if Foo then begin");
    builder.appendImpl("    Bar;");
    builder.appendImpl("  end;");
    builder.appendImpl("end;");
    builder.appendImpl("procedure TEmptyProcs.Three;");
    builder.appendImpl("begin");
    builder.appendImpl("  if Foo then begin");
    builder.appendImpl("    // Do nothing");
    builder.appendImpl("  end;");
    builder.appendImpl("end;");
    builder.appendImpl("procedure GlobalProcedureFour;");
    builder.appendImpl("begin");
    builder.appendImpl("  Writeln('OK');");
    builder.appendImpl("end;");
    builder.appendImpl("procedure GlobalProcedureFive;");
    builder.appendImpl("begin");
    builder.appendImpl("  if Foo then begin");
    builder.appendImpl("    // Do nothing");
    builder.appendImpl("  end;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(2));
    assertIssues(hasItem(hasRuleKeyAtLine("EmptyBeginStatementRule", builder.getOffSet() + 13)));
    assertIssues(hasItem(hasRuleKeyAtLine("EmptyBeginStatementRule", builder.getOffSet() + 23)));
  }
  @Test
  public void testEmptyMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("type");
    builder.appendDecl("  TEmptyProcs = class");
    builder.appendDecl("  public");
    builder.appendDecl("    procedure One;");
    builder.appendDecl("  end;");

    builder.appendImpl("procedure TEmptyProcs.One;");
    builder.appendImpl("begin");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(not(hasItem(hasRuleKeyAtLine("EmptyBeginStatementRule", builder.getOffSet() + 2))));
  }
}
