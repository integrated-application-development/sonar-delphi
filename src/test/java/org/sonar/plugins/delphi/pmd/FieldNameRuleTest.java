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

public class FieldNameRuleTest extends BasePmdRuleTest {

  @Test
  public void testValidRule() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("  private");
    builder.appendDecl("   FFoo: Integer;");
    builder.appendDecl("  protected");
    builder.appendDecl("   FBar: String;");
    builder.appendDecl("  end;");

    execute(builder);

    assertThat(stringifyIssues(), issues, is(empty()));
  }

/*  @Test
  public void fieldNameWithoutPrefixShouldAddIssue() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("    private");
    builder.appendDecl("     Id: Integer;");
    builder.appendDecl("     Code: Integer;");
    builder.appendDecl("    protected");
    builder.appendDecl("     Name: String;");
    builder.appendDecl("  end;");

    testAnalyse(builder);

    assertThat(issues, hasItem(hasRuleKeyAtLine("FieldNameRule", builder.getOffsetDecl() + 4)));
    assertThat(issues, hasItem(hasRuleKeyAtLine("FieldNameRule", builder.getOffsetDecl() + 5)));
    assertThat(issues, hasItem(hasRuleKeyAtLine("FieldNameRule", builder.getOffsetDecl() + 7)));
  }*/

/*  @Test
  public void publishedFieldsShouldBeSkipped() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("     DefaultId: Integer;");
    builder.appendDecl("    private");
    builder.appendDecl("     Id: Integer;");
    builder.appendDecl("    protected");
    builder.appendDecl("     Name: String;");
    builder.appendDecl("    public");
    builder.appendDecl("     Name: String;");
    builder.appendDecl("  end;");

    testAnalyse(builder);

    assertThat(issues, not(hasItem(hasRuleLine(builder.getOffsetDecl() + 3))));
    assertThat(issues, hasItem(hasRuleKeyAtLine("FieldNameRule", builder.getOffsetDecl() + 5)));
    assertThat(issues, hasItem(hasRuleKeyAtLine("FieldNameRule", builder.getOffsetDecl() + 7)));
    assertThat(issues, hasItem(hasRuleKeyAtLine("FieldNameRule", builder.getOffsetDecl() + 9)));
  }*/

/*  @Test
  public void fieldNameDoNotStartsWithCapitalLetterShouldAddIssue() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("    private");
    builder.appendDecl("     Ffoo: Integer;");
    builder.appendDecl("    protected");
    builder.appendDecl("     Fbar: String;");
    builder.appendDecl("  end;");

    testAnalyse(builder);

    assertThat(issues, hasSize(2));
    assertThat(issues, hasItem(allOf(hasRuleKey("FieldNameRule"), hasRuleLine(builder.getOffsetDecl() + 4))));
    assertThat(issues, hasItem(allOf(hasRuleKey("FieldNameRule"), hasRuleLine(builder.getOffsetDecl() + 6))));
  }*/

/*  @Test
  public void testAvoidFalsePositive() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("    private");
    builder.appendDecl("     Foo: Integer;");
    builder.appendDecl("  end;");

    testAnalyse(builder);

    assertThat(issues, hasSize(1));
    assertThat(issues, hasItem(allOf(hasRuleKey("FieldNameRule"), hasRuleLine(builder.getOffsetDecl() + 4))));
  }

  @Test
  public void testOneLetterNameField() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("    private");
    builder.appendDecl("     x: Integer;");
    builder.appendDecl("  end;");

    testAnalyse(builder);

    assertThat(issues, hasSize(1));
    assertThat(issues, hasItem(hasRuleKeyAtLine("FieldNameRule", builder.getOffsetDecl() + 4)));
  }*/

}
