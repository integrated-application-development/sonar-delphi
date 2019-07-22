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
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class TypeAliasRuleTest extends BasePmdRuleTest {

  @Test
  public void testTypeAliasShouldAddViolation() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("type");
    builder.appendDecl("  TMyChar = Char;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("TypeAliasRule", builder.getOffsetDecl() + 2)));
  }

  @Test
  public void testTypeAliasNewTypeShouldAddViolation() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("type");
    builder.appendDecl("  TMyChar = type Char;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("TypeAliasRule", builder.getOffsetDecl() + 2)));
  }

  @Test
  public void testFalsePositiveMetaClassIsNotTypeAlias() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("  end;");
    builder.appendDecl("  TMetaClass = class of TMyClass;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testFalsePositiveEmptyRecordIsNotTypeAlias() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("type");
    builder.appendDecl("  TMyRecord = record");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testFalsePositiveEmptyClassIsNotTypeAlias() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testFalsePositiveSetsAreNotTypeAlias() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("type");
    builder.appendDecl("  TSetOfChar = set of Char;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testFalsePositiveArraysAreNotTypeAlias() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("type");
    builder.appendDecl("  TArrayOfChar = array of Char;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testFalsePositiveSubRangesAreNotTypeAlias() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("type");
    builder.appendDecl("  TSubRange = Lower..Upper;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testFalsePositivePointerTypesAreNotTypeAlias() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("type");
    builder.appendDecl("  PClassPointer = ^TClass;");

    execute(builder);

    assertIssues(empty());
  }


}
