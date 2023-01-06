/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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

class PublicFieldsRuleTest extends BasePmdRuleTest {

  @Test
  void testValidRule() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyClass = class(TObject)")
            .appendDecl("     FPublishedField: Integer;")
            .appendDecl("    private")
            .appendDecl("     FPrivateField: Integer;")
            .appendDecl("    protected")
            .appendDecl("     FProtectedField: String;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("PublicFieldsRule"));
  }

  @Test
  void testInvalidRule() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyClass = class(TObject)")
            .appendDecl("     FPublishedField: Integer;")
            .appendDecl("    private")
            .appendDecl("     FPrivateField: Integer;")
            .appendDecl("    protected")
            .appendDecl("     FProtectedField: String;")
            .appendDecl("    public")
            .appendDecl("     FPublicField: String;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("PublicFieldsRule", builder.getOffsetDecl() + 9));
  }

  @Test
  void testRecordsAreExcluded() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyRecord = record")
            .appendDecl("     FPublishedField: Integer;")
            .appendDecl("    private")
            .appendDecl("     FPrivateField: Integer;")
            .appendDecl("    protected")
            .appendDecl("     FProtectedField: String;")
            .appendDecl("    public")
            .appendDecl("     FPublicField: String;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("PublicFieldsRule"));
  }
}
