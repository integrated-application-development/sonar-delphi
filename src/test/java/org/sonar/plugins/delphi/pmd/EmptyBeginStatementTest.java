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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.sonar.plugins.delphi.IssueMatchers.*;

public class EmptyBeginStatementTest extends BasePmdRuleTest {

  @Test
  public void validRule() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();

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

    analyse(builder);

    assertThat(issues, is(empty()));
  }

  @Test
  public void testBeginEndStatement() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();

    builder.appendDecl("type");
    builder.appendDecl("  TEmptyProcs = class");
    builder.appendDecl("  public");
    builder.appendDecl("    procedure One;");
    builder.appendDecl("    procedure Two;");
    builder.appendDecl("    procedure Three;");
    builder.appendDecl("    procedure Four;");
    builder.appendDecl("    procedure Five;");
    builder.appendDecl("  end;");

    builder.appendImpl("procedure TEmptyProcs.One;");
    builder.appendImpl("begin");
    builder.appendImpl("end;");
    builder.appendImpl("procedure TEmptyProcs.Two;");
    builder.appendImpl("begin");
    builder.appendImpl("end;");
    builder.appendImpl("procedure TEmptyProcs.Three;");
    builder.appendImpl("begin");
    builder.appendImpl("end;");
    builder.appendImpl("procedure TEmptyProcs.Four;");
    builder.appendImpl("begin");
    builder.appendImpl("end;");
    builder.appendImpl("procedure TEmptyProcs.Five;");
    builder.appendImpl("begin");
    builder.appendImpl("end;");

    analyse(builder);

    assertThat(toString(issues), issues, hasSize(5));
    assertThat(toString(issues), issues, hasItem(allOf(hasRuleKey("EmptyBeginStatementRule"), hasRuleLine(builder.getOffSet() + 2))));
    assertThat(toString(issues), issues, hasItem(allOf(hasRuleKey("EmptyBeginStatementRule"), hasRuleLine(builder.getOffSet() + 5))));
    assertThat(toString(issues), issues, hasItem(allOf(hasRuleKey("EmptyBeginStatementRule"), hasRuleLine(builder.getOffSet() + 8))));
    assertThat(toString(issues), issues, hasItem(allOf(hasRuleKey("EmptyBeginStatementRule"), hasRuleLine(builder.getOffSet() + 11))));
    assertThat(toString(issues), issues, hasItem(allOf(hasRuleKey("EmptyBeginStatementRule"), hasRuleLine(builder.getOffSet() + 14))));
  }

}
