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

class ForbiddenPropertyCheckTest {
  private static final String UNIT_NAME = "TestUnit";
  private static final String FORBIDDEN_PROPERTY = "TestUnit.TFoo.Bar";

  private static DelphiCheck createCheck() {
    ForbiddenPropertyCheck check = new ForbiddenPropertyCheck();
    check.properties = FORBIDDEN_PROPERTY;
    return check;
  }

  @Test
  void testForbiddenPropertyUsageShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .unitName(UNIT_NAME)
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    FBar: TFoo;")
                .appendDecl("    property Bar: TFoo read FBar;")
                .appendDecl("  end;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: TFoo;")
                .appendImpl("begin")
                .appendImpl("  Foo := TFoo.Create;")
                .appendImpl("  Foo := TFoo.Bar;")
                .appendImpl("end;"))
        .verifyIssueOnLine(18);
  }

  @Test
  void testNotUsingForbiddenPropertyShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .unitName(UNIT_NAME)
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    FBar: TFoo;")
                .appendDecl("    property Bar: TFoo read FBar;")
                .appendDecl("    property Baz: TFoo read FBar;")
                .appendDecl("  end;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: TFoo;")
                .appendImpl("begin")
                .appendImpl("  Foo := Local(TFoo.Create).Obj as TFoo;")
                .appendImpl("  Foo := Foo.Baz;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
