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

class ForbiddenTypeCheckTest {
  private static final String UNIT_NAME = "TestUnit";
  private static final String FORBIDDEN_TYPES = "TestUnit.TFoo,TestUnit.TFoo.TBar";

  private static DelphiCheck createCheck() {
    ForbiddenTypeCheck check = new ForbiddenTypeCheck();
    check.blacklist = FORBIDDEN_TYPES;
    return check;
  }

  @Test
  void testForbiddenTypeUsageShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .unitName(UNIT_NAME)
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    class procedure Bar;")
                .appendDecl("  end;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: TFoo;")
                .appendImpl("begin")
                .appendImpl("  TFoo.Bar;")
                .appendImpl("end;"))
        .verifyIssueOnLine(14, 16);
  }

  @Test
  void testForbiddenNestedTypeUsageShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .unitName(UNIT_NAME)
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  type")
                .appendDecl("    TNested = class(TObject)")
                .appendDecl("      class procedure Bar;")
                .appendDecl("    end;")
                .appendDecl("    class procedure Bar;")
                .appendDecl("  end;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: TFoo;")
                .appendImpl("  Nested: TFoo.TNested;")
                .appendImpl("begin")
                .appendImpl("  TFoo.Bar;")
                .appendImpl("  TFoo.TNested.Bar;")
                .appendImpl("end;"))
        .verifyIssueOnLine(18, 19, 21, 22);
  }

  @Test
  void testMethodImplementationShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .unitName(UNIT_NAME)
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    procedure Bar; virtual;")
                .appendDecl("  end;")
                .appendImpl("procedure TFoo.Bar;")
                .appendImpl("begin")
                .appendImpl("  // Do nothing")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
