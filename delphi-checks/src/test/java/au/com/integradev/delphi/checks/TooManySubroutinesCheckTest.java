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

class TooManySubroutinesCheckTest {
  @Test
  void testNotTooManySubroutinesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooManySubroutinesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo: Integer;")
                .appendImpl("  function Bar: Integer;")
                .appendImpl("  begin")
                .appendImpl("    Result := 1;")
                .appendImpl("  end;")
                .appendImpl("  function Baz: Integer;")
                .appendImpl("  begin")
                .appendImpl("    Result := 2;")
                .appendImpl("  end;")
                .appendImpl("  function Qux: Integer;")
                .appendImpl("  begin")
                .appendImpl("    Result := 3;")
                .appendImpl("  end;")
                .appendImpl("begin")
                .appendImpl("  Result := Bar + Baz + Qux;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testTooManySubroutinesShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooManySubroutinesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo: Integer;")
                .appendImpl("  function Bar: Integer;")
                .appendImpl("  begin")
                .appendImpl("    Result := 1;")
                .appendImpl("  end;")
                .appendImpl("  function Baz: Integer;")
                .appendImpl("  begin")
                .appendImpl("    Result := 2;")
                .appendImpl("  end;")
                .appendImpl("  function Qux: Integer;")
                .appendImpl("  begin")
                .appendImpl("    Result := 3;")
                .appendImpl("  end;")
                .appendImpl("  function Xyzzy: Integer;")
                .appendImpl("  begin")
                .appendImpl("    Result := 4;")
                .appendImpl("  end;")
                .appendImpl("begin")
                .appendImpl("  Result := Bar + Baz + Qux + Xyzzy;")
                .appendImpl("end;"))
        .verifyIssueOnLine(7);
  }

  @Test
  void testTooManyConstructorSubProceduresShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooManySubroutinesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("constructor TMyObject.Create;")
                .appendImpl("  function Bar: Integer;")
                .appendImpl("  begin")
                .appendImpl("    Result := 1;")
                .appendImpl("  end;")
                .appendImpl("  function Baz: Integer;")
                .appendImpl("  begin")
                .appendImpl("    Result := 2;")
                .appendImpl("  end;")
                .appendImpl("  function Qux: Integer;")
                .appendImpl("  begin")
                .appendImpl("    Result := 3;")
                .appendImpl("  end;")
                .appendImpl("  function Xyzzy: Integer;")
                .appendImpl("  begin")
                .appendImpl("    Result := 4;")
                .appendImpl("  end;")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("  FMyField := Bar + Baz + Qux + Xyzzy;")
                .appendImpl("end;"))
        .verifyIssueOnLine(7);
  }

  @Test
  void testTooManyDestructorSubProceduresShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooManySubroutinesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("destructor TMyObject.Destroy;")
                .appendImpl("  function Bar: Integer;")
                .appendImpl("  begin")
                .appendImpl("    Result := 1;")
                .appendImpl("  end;")
                .appendImpl("  function Baz: Integer;")
                .appendImpl("  begin")
                .appendImpl("    Result := 2;")
                .appendImpl("  end;")
                .appendImpl("  function Qux: Integer;")
                .appendImpl("  begin")
                .appendImpl("    Result := 3;")
                .appendImpl("  end;")
                .appendImpl("  function Xyzzy: Integer;")
                .appendImpl("  begin")
                .appendImpl("    Result := 4;")
                .appendImpl("  end;")
                .appendImpl("begin")
                .appendImpl("  FMyField := Bar + Baz + Qux + Xyzzy;")
                .appendImpl("  inherited;")
                .appendImpl("end;"))
        .verifyIssueOnLine(7);
  }

  @Test
  void testTooManyNestedProceduresShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooManySubroutinesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("  function Bar: Integer;")
                .appendImpl("    function Baz: Integer;")
                .appendImpl("      function Qux: Integer;")
                .appendImpl("        function Xyzzy: Integer;")
                .appendImpl("        begin")
                .appendImpl("          Result := 4;")
                .appendImpl("        end;")
                .appendImpl("      begin")
                .appendImpl("        Result := Xyzzy;")
                .appendImpl("      end;")
                .appendImpl("    begin")
                .appendImpl("      Result := Qux;")
                .appendImpl("    end;")
                .appendImpl("  begin")
                .appendImpl("    Result := Baz;")
                .appendImpl("  end;")
                .appendImpl("begin")
                .appendImpl("  FMyField := Bar;")
                .appendImpl("end;"))
        .verifyIssueOnLine(7);
  }
}
