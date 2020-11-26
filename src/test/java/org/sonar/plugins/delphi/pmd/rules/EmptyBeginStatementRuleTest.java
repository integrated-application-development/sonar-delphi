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

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class EmptyBeginStatementRuleTest extends BasePmdRuleTest {
  @Test
  void testValidRule() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TEmptyProcs = class(TObject)")
            .appendDecl("  public")
            .appendDecl("    procedure One;")
            .appendDecl("    procedure Two;")
            .appendDecl("    procedure Three;")
            .appendDecl("  end;")
            .appendImpl("procedure TEmptyProcs.One;")
            .appendImpl("begin")
            .appendImpl("  WriteLn('OK');")
            .appendImpl("end;")
            .appendImpl("procedure TEmptyProcs.Two;")
            .appendImpl("begin")
            .appendImpl("  WriteLn('OK');")
            .appendImpl("end;")
            .appendImpl("procedure TEmptyProcs.Three;")
            .appendImpl("begin")
            .appendImpl("  WriteLn('OK');")
            .appendImpl("end;")
            .appendImpl("procedure GlobalProcedureFour;")
            .appendImpl("begin")
            .appendImpl("  WriteLn('OK');")
            .appendImpl("end;")
            .appendImpl("procedure GlobalProcedureFive;")
            .appendImpl("begin")
            .appendImpl("  WriteLn('OK');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testEmptyBeginStatements() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TEmptyProcs = class(TObject)")
            .appendDecl("  public")
            .appendDecl("    procedure One;")
            .appendDecl("    procedure Two;")
            .appendDecl("    procedure Three;")
            .appendDecl("  end;")
            .appendImpl("procedure TEmptyProcs.One;")
            .appendImpl("begin")
            .appendImpl("  WriteLn('OK');")
            .appendImpl("end;")
            .appendImpl("procedure TEmptyProcs.Two;")
            .appendImpl("begin")
            .appendImpl("  if Foo then begin")
            .appendImpl("    Bar;")
            .appendImpl("  end;")
            .appendImpl("end;")
            .appendImpl("procedure TEmptyProcs.Three;")
            .appendImpl("begin")
            .appendImpl("  if Foo then begin")
            .appendImpl("    // Do nothing")
            .appendImpl("  end;")
            .appendImpl("end;")
            .appendImpl("procedure GlobalProcedureFour;")
            .appendImpl("begin")
            .appendImpl("  WriteLn('OK');")
            .appendImpl("end;")
            .appendImpl("procedure GlobalProcedureFive;")
            .appendImpl("begin")
            .appendImpl("  if Foo then begin")
            .appendImpl("    // Do nothing")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(2)
        .areExactly(1, ruleKeyAtLine("EmptyBeginStatementRule", builder.getOffset() + 13))
        .areExactly(1, ruleKeyAtLine("EmptyBeginStatementRule", builder.getOffset() + 23));
  }

  @Test
  void testEmptyBlocksInCaseStatementShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test(Arg: Integer);")
            .appendImpl("begin")
            .appendImpl("  case Arg of")
            .appendImpl("    0: begin")
            .appendImpl("    end;")
            .appendImpl("  else begin")
            .appendImpl("  end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("EmptyBeginStatementRule", builder.getOffset() + 4))
        .areExactly(1, ruleKeyAtLine("EmptyBeginStatementRule", builder.getOffset() + 6));
  }

  @Test
  void testEmptyBlocksInCaseStatementShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test(Arg: Integer);")
            .appendImpl("begin")
            .appendImpl("  case Arg of")
            .appendImpl("    0: begin")
            .appendImpl("      // Do nothing")
            .appendImpl("    end;")
            .appendImpl("  else begin")
            .appendImpl("    // Do nothing")
            .appendImpl("  end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyBeginStatementRule"));
  }

  @Test
  void testEmptyMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TEmptyProcs = class")
            .appendDecl("  public")
            .appendDecl("    procedure One;")
            .appendDecl("  end;")
            .appendImpl("procedure TEmptyProcs.One;")
            .appendImpl("begin")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("EmptyBeginStatementRule", builder.getOffset() + 2));
  }
}
