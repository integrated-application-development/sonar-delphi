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
package au.com.integradev.delphi.checks;

import static au.com.integradev.delphi.conditions.RuleKey.ruleKey;
import static au.com.integradev.delphi.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.CheckTest;
import au.com.integradev.delphi.builders.DelphiTestProgramBuilder;
import org.junit.jupiter.api.Test;

class ProjectFileVariableCheckTest extends CheckTest {
  @Test
  void testValidDprFile() {
    DelphiTestProgramBuilder builder =
        new DelphiTestProgramBuilder().programName("ValidProgram").appendImpl("Exit;");

    execute(builder);

    assertIssues().areNot(ruleKey("ProjectFileNoVariablesRule"));
  }

  @Test
  void testVariablesInDprFileShouldAddIssue() {
    DelphiTestProgramBuilder builder =
        new DelphiTestProgramBuilder()
            .appendDecl("var")
            .appendDecl("  GMyString: String;")
            .appendDecl("  GMyBool: Boolean;")
            .appendDecl("  GMyInt: Integer;")
            .appendImpl("Exit;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ProjectFileNoVariablesRule", builder.getOffsetDecl() + 2))
        .areExactly(1, ruleKeyAtLine("ProjectFileNoVariablesRule", builder.getOffsetDecl() + 3))
        .areExactly(1, ruleKeyAtLine("ProjectFileNoVariablesRule", builder.getOffsetDecl() + 4));
  }

  @Test
  void testVarsInsideMethodsShouldNotAddIssue() {
    DelphiTestProgramBuilder builder =
        new DelphiTestProgramBuilder()
            .appendDecl("procedure MyProcedure;")
            .appendDecl("var")
            .appendDecl("  MyString: String;")
            .appendDecl("  MyBool: Boolean;")
            .appendDecl("  MyInt: Integer;")
            .appendDecl("begin")
            .appendDecl("  DoSomething;")
            .appendDecl("end;")
            .appendDecl("function MyFunction;")
            .appendDecl("var")
            .appendDecl("  MyString: String;")
            .appendDecl("  MyBool: Boolean;")
            .appendDecl("  MyInt: Integer;")
            .appendDecl("begin")
            .appendDecl("  Result := 5;")
            .appendDecl("end;")
            .appendImpl("MyProcedure;")
            .appendImpl("MyVar := MyFunction;");

    execute(builder);

    assertIssues()
        .areNot(ruleKeyAtLine("ProjectFileNoVariablesRule", builder.getOffsetDecl() + 2))
        .areNot(ruleKeyAtLine("ProjectFileNoVariablesRule", builder.getOffsetDecl() + 6));
  }
}
