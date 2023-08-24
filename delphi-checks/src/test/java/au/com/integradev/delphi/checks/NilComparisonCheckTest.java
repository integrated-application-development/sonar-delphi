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

class NilComparisonCheckTest {
  @Test
  void testAssignedCheckShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NilComparisonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test(Foo: TObject);")
                .appendImpl("begin")
                .appendImpl("  if Assigned(Foo) then begin")
                .appendImpl("    Exit;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNonVariablesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NilComparisonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBaz = class(TObject)")
                .appendDecl("    function Baz: String;")
                .appendDecl("  end;")
                .appendImpl("function Bar: TObject;")
                .appendImpl("begin")
                .appendImpl("end;")
                .appendImpl("procedure Test(BazVar: TBaz);")
                .appendImpl("begin")
                .appendImpl("  if BazVar.Baz = nil then begin")
                .appendImpl("    Exit;")
                .appendImpl("  end;")
                .appendImpl("  if Bar = nil then begin")
                .appendImpl("    Exit;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRegularComparisonsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NilComparisonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test(Foo: TObject; Bar: TObject);")
                .appendImpl("begin")
                .appendImpl("  if Foo = Bar then begin")
                .appendImpl("    Exit;")
                .appendImpl("  end;")
                .appendImpl("  if Foo <> Bar then begin")
                .appendImpl("    Exit;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNilComparisonsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NilComparisonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBar = class(TObject)")
                .appendDecl("    class var FBar: TObject")
                .appendDecl("  end;")
                .appendImpl("procedure Test(Foo: TObject);")
                .appendImpl("begin")
                .appendImpl("  if TBar.FBar = nil then begin")
                .appendImpl("    Exit;")
                .appendImpl("  end;")
                .appendImpl("  if Foo = nil then begin")
                .appendImpl("    Exit;")
                .appendImpl("  end;")
                .appendImpl("  if Foo <> ((nil)) then begin")
                .appendImpl("    Exit;")
                .appendImpl("  end;")
                .appendImpl("  if (nil) = Foo then begin")
                .appendImpl("    Exit;")
                .appendImpl("  end;")
                .appendImpl("  if nil <> Foo then begin")
                .appendImpl("    Exit;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(14, 17, 20, 23, 26);
  }
}
