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
package au.com.integradev.delphi.checks;

import static au.com.integradev.delphi.conditions.RuleKey.ruleKey;
import static au.com.integradev.delphi.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.CheckTest;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import org.junit.jupiter.api.Test;

class ClassNameCheckTest extends CheckTest {

  @Test
  void testClassNameWithPrefixShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(TObject)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ClassNameRule"));
  }

  @Test
  void testClassNameWithWrongCasePrefixShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  tType = class(TObject)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("ClassNameRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testNestedClassesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TOuterClass = class(TObject)");
    builder.appendDecl("  strict private");
    builder.appendDecl("    type");
    builder.appendDecl("      TInnerClass1 = class(TObject)");
    builder.appendDecl("      end;");
    builder.appendDecl("      TInnerClass2 = class(TObject)");
    builder.appendDecl("      end;");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ClassNameRule"));
  }

  @Test
  void testAttributeClassNameShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("uses System;");
    builder.appendDecl("type");
    builder.appendDecl("  my_attribute = class(TCustomAttribute)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ClassNameRule"));
  }

  @Test
  void testClassHelperNameShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  my_helper = class helper for TObject");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ClassNameRule"));
  }
}
