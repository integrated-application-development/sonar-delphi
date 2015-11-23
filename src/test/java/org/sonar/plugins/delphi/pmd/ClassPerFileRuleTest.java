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

public class ClassPerFileRuleTest extends BasePmdRuleTest {

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
  public void moreThanOneClassShouldAddIssue() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("  end;");
    builder.appendDecl("  TMyClass2 = class");
    builder.appendDecl("  end;");

    analyse(builder);

    assertThat(issues, hasSize(1));
    assertThat(issues, hasItem(hasRuleKey("OneClassPerFileRule")));
    assertThat(issues, hasItem(hasRuleLine(builder.getOffsetDecl() + 4)));
  }

  @Test
  public void falsePositiveMetaClass() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("  end;");
    builder.appendDecl("  TMetaClassClass = class of TMyClass;");

    analyse(builder);

    assertThat(toString(issues), issues, is(empty()));
  }

  @Test
  public void falsePositiveClassMethods() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("    class procedure TestProcedure;");
    builder.appendDecl("    class function TestFuncion: Boolean;");
    builder.appendDecl("  end;");

    analyse(builder);

    assertThat(toString(issues), issues, is(empty()));
  }

}
