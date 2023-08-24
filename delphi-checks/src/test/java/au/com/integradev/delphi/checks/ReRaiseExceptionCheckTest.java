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

class ReRaiseExceptionCheckTest {
  @Test
  void testRaiseInExceptShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ReRaiseExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    ThrowException;")
                .appendImpl("  except")
                .appendImpl("    raise;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRaiseInExceptionHandlerShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ReRaiseExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    ThrowException;")
                .appendImpl("  except")
                .appendImpl("    on E: MyException do begin")
                .appendImpl("      raise;")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRaisingNormalExceptionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ReRaiseExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure ThrowException;")
                .appendImpl("begin")
                .appendImpl("  raise Exception.Create('Spooky scary skeletons!');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRaiseInExceptionHandlerWithNoSemicolonOrBeginEndShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ReRaiseExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    ThrowException;")
                .appendImpl("  except")
                .appendImpl("    on E: Exception do raise")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testBadRaiseShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ReRaiseExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    ThrowException;")
                .appendImpl("  except")
                .appendImpl("    on E: MyException do begin")
                .appendImpl("      raise E;")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(13);
  }

  @Test
  void testBadRaiseWithNoSemicolonOrBeginEndShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ReRaiseExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    ThrowException;")
                .appendImpl("  except")
                .appendImpl("    on E: Exception do raise E")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(12);
  }

  @Test
  void testMultipleBadRaisesShouldAddIssues() {
    CheckVerifier.newVerifier()
        .withCheck(new ReRaiseExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    ThrowException;")
                .appendImpl("  except")
                .appendImpl("    on E: MyException do begin")
                .appendImpl("      if SomeCondition then begin")
                .appendImpl("        raise E;")
                .appendImpl("      end;")
                .appendImpl("      raise E;")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(14, 16);
  }

  @Test
  void testRaiseDifferentExceptionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ReRaiseExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    ThrowException;")
                .appendImpl("  except")
                .appendImpl("    on E: MyException do begin")
                .appendImpl("      raise SomeOtherException.Create;")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRaiseDifferentExceptionWithoutIdentifierShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ReRaiseExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    ThrowException;")
                .appendImpl("  except")
                .appendImpl("    on Exception do begin")
                .appendImpl("      raise Exception.Create;")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
