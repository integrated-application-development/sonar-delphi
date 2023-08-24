/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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

class ForbiddenEnumValueCheckTest {
  private static final String UNIT_NAME = "Foo";
  private static final String ENUM_NAME = UNIT_NAME + ".TBar";
  private static final String FORBIDDEN_VALUE = "Baz";

  private static DelphiCheck createCheck() {
    ForbiddenEnumValueCheck check = new ForbiddenEnumValueCheck();
    check.enumName = ENUM_NAME;
    check.blacklist = FORBIDDEN_VALUE;
    return check;
  }

  @Test
  void testForbiddenEnumValueShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .unitName(UNIT_NAME)
                .appendDecl("type")
                .appendDecl("  TBar = (Baz, Beep, Boop, Blop);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  Bar := Baz;")
                .appendImpl("end;"))
        .verifyIssueOnLine(14);
  }

  @Test
  void testQualifiedForbiddenEnumValueShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .unitName(UNIT_NAME)
                .appendDecl("type")
                .appendDecl("  TBar = (Baz, Beep, Boop, Blop);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  Bar := TBar.Baz;")
                .appendImpl("end;"))
        .verifyIssueOnLine(14);
  }

  @Test
  void testAllowedEnumValueShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .unitName(UNIT_NAME)
                .appendDecl("type")
                .appendDecl("  TBar = (Baz, Beep, Boop, Blop);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  Bar := Beep;")
                .appendImpl("  Bar := Boop;")
                .appendImpl("  Bar := Blop;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
