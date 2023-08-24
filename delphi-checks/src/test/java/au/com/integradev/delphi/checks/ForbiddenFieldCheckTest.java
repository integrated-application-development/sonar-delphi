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

class ForbiddenFieldCheckTest {
  private static final String UNIT_NAME = "TestUnit";
  private static final String DECLARING_TYPE = UNIT_NAME + ".TFoo";
  private static final String FORBIDDEN_FIELD = "Bar";

  private static DelphiCheck createCheck() {
    ForbiddenFieldCheck check = new ForbiddenFieldCheck();
    check.declaringType = DECLARING_TYPE;
    check.blacklist = FORBIDDEN_FIELD;
    return check;
  }

  @Test
  void testForbiddenFieldUsageShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .unitName(UNIT_NAME)
                .appendDecl("type")
                .appendDecl("  TFoo = record")
                .appendDecl("    Bar: String;")
                .appendDecl("  end;")
                .appendImpl("procedure Test(Foo: TFoo);")
                .appendImpl("var")
                .appendImpl("  Baz: String;")
                .appendImpl("begin")
                .appendImpl("  Baz := Foo.Bar;")
                .appendImpl("end;"))
        .verifyIssueOnLine(16);
  }

  @Test
  void testForbiddenFieldNameDeclaredByDifferentTypeShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .unitName(UNIT_NAME)
                .appendDecl("type")
                .appendDecl("  TBoop = record")
                .appendDecl("    Bar: String;")
                .appendDecl("  end;")
                .appendImpl("procedure Test(Boop: TBoop);")
                .appendImpl("var")
                .appendImpl("  Baz: String;")
                .appendImpl("begin")
                .appendImpl("  Baz := Boop.Bar;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
