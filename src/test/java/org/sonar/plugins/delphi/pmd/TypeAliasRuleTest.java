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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class TypeAliasRuleTest extends BasePmdRuleTest {

  @Test
  public void testSetsArentTypeAlias() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();

    builder.appendDecl("type");
    builder.appendDecl("  TMySetOfChar = set of Char;");

    execute(builder);

    assertThat(stringifyIssues(), issues, is(empty()));
  }

/*  @Test
  public void typeAliasShouldAddViolation() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();

    builder.appendDecl("type");
    builder.appendDecl("  TMyChar = Char;");

    testAnalyse(builder);

    assertThat(toString(issues), issues, hasSize(1));
    assertThat(toString(issues), issues, hasItem(allOf(hasRuleKey("TypeAliasRule"), hasRuleLine(builder.getOffsetDecl() + 2))));
  }*/

/*  @Test
  public void typeAliasNewTypeShouldAddViolation() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();

    builder.appendDecl("type");
    builder.appendDecl("  TMyChar = type Char;");

    testAnalyse(builder);

    assertThat(toString(issues), issues, hasSize(1));
    assertThat(toString(issues), issues, hasItem(allOf(hasRuleKey("TypeAliasRule"), hasRuleLine(builder.getOffsetDecl() + 2))));
  }*/

  @Test
  public void testFalsePositiveMetaClassIsNotTypeAlias() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();

    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("  end;");
    builder.appendDecl("  TMetaClass = class of TMyClass;");

    execute(builder);

    assertThat(stringifyIssues(), issues, is(empty()));
  }

  @Test
  public void testFalsePositiveEmptyRecordIsNotTypeAlias() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();

    builder.appendDecl("type");
    builder.appendDecl("  TMyRecord = record");
    builder.appendDecl("  end;");

    execute(builder);

    assertThat(stringifyIssues(), issues, is(empty()));
  }

  @Test
  public void testFalsePositiveEmptyClassIsNotTypeAlias() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();

    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("  end;");

    execute(builder);

    assertThat(stringifyIssues(), issues, is(empty()));
  }

}
