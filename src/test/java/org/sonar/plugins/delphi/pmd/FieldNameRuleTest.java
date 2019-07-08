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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

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

    assertIssues(empty());
  }

  @Test
  public void testFieldNameWithoutPrefixShouldAddIssue() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("    private");
    builder.appendDecl("     Id: Integer;");
    builder.appendDecl("     Code: Integer;");
    builder.appendDecl("    protected");
    builder.appendDecl("     Name: String;");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(hasItem(hasRuleKeyAtLine("FieldNameRule", builder.getOffsetDecl() + 4)));
    assertIssues(hasItem(hasRuleKeyAtLine("FieldNameRule", builder.getOffsetDecl() + 5)));
    assertIssues(hasItem(hasRuleKeyAtLine("FieldNameRule", builder.getOffsetDecl() + 7)));
  }

  @Test
  public void testPublishedFieldsShouldBeSkipped() {
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

    execute(builder);

    assertIssues(not(hasItem(hasRuleKeyAtLine("FieldNameRule", builder.getOffsetDecl() + 3))));
    assertIssues(hasItem(hasRuleKeyAtLine("FieldNameRule", builder.getOffsetDecl() + 5)));
    assertIssues(hasItem(hasRuleKeyAtLine("FieldNameRule", builder.getOffsetDecl() + 7)));
    assertIssues(hasItem(hasRuleKeyAtLine("FieldNameRule", builder.getOffsetDecl() + 9)));
  }

  @Test
  public void testBadPascalCaseShouldAddIssue() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("    private");
    builder.appendDecl("     Ffoo: Integer;");
    builder.appendDecl("     Foo: Integer;");
    builder.appendDecl("    protected");
    builder.appendDecl("     Fbar: String;");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(hasSize(3));
    assertIssues(hasItem(hasRuleKeyAtLine(("FieldNameRule"), builder.getOffsetDecl() + 4)));
    assertIssues(hasItem(hasRuleKeyAtLine(("FieldNameRule"), builder.getOffsetDecl() + 5)));
    assertIssues(hasItem(hasRuleKeyAtLine("FieldNameRule", builder.getOffsetDecl() + 7)));
  }

  @Test
  public void testOneLetterNameFields() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("    private");
    builder.appendDecl("     x: Integer;");
    builder.appendDecl("     F: Integer;");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(hasSize(2));
    assertIssues(hasItem(hasRuleKeyAtLine("FieldNameRule", builder.getOffsetDecl() + 4)));
    assertIssues(hasItem(hasRuleKeyAtLine("FieldNameRule", builder.getOffsetDecl() + 5)));
  }
}
