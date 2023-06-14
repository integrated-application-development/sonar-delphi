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

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

class ConstantNameCheckTest {
  private static DelphiCheck createCheck() {
    ConstantNameCheck check = new ConstantNameCheck();
    check.prefixes = "C";
    return check;
  }

  @Test
  void testConstantWithPrefixShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("const")
                .appendDecl("  CMyConstant = 'Value';"))
        .verifyNoIssues();
  }

  @Test
  void testFirstCharacterIsNumberShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("const")
                .appendDecl("  C85Constant = 'Value';"))
        .verifyNoIssues();
  }

  @Test
  void testBadPrefixShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("const")
                .appendDecl("  C_MyConstant = 'Value';"))
        .verifyIssueOnLine(6);
  }

  @Test
  void testBadPascalCaseShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("const")
                .appendDecl("  CmyConstant = 'Value';"))
        .verifyIssueOnLine(6);
  }

  @Test
  void testInlineConstantWithPrefixShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  const CMyConstant = 'Value';")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testInlineFirstCharacterIsNumberShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  const C85Constant = 'Value';")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testInlineBadPrefixShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  const C_Constant = 'Value';")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }

  @Test
  void testInlineBadPascalCaseShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  const CmyConstant = 'Value';")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }
}
