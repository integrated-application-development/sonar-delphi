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

class SwallowedExceptionCheckTest {
  @Test
  void testExceptBlockShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new SwallowedExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    ThrowException;")
                .appendImpl("  except")
                .appendImpl("    Log.Debug('except block');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testHandlerShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new SwallowedExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    ThrowException;")
                .appendImpl("  except")
                .appendImpl("    on E: MyException do begin")
                .appendImpl("      Log.Debug('exception handler');")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testEmptyElseShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new SwallowedExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    ThrowException;")
                .appendImpl("  except")
                .appendImpl("    on E: MyException do begin")
                .appendImpl("      Log.Debug('exception handler');")
                .appendImpl("    end;")
                .appendImpl("    else begin")
                .appendImpl("      // Do nothing")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(15);
  }

  @Test
  void testBareElseShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new SwallowedExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    ThrowException;")
                .appendImpl("  except")
                .appendImpl("    on E: MyException do begin")
                .appendImpl("      Log.Debug('exception handler');")
                .appendImpl("    end;")
                .appendImpl("    else")
                .appendImpl("      // Do nothing")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(15);
  }

  @Test
  void testElseWithSingleStatementShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new SwallowedExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    ThrowException;")
                .appendImpl("  except")
                .appendImpl("    on E: MyException do begin")
                .appendImpl("      Log.Debug('exception handler');")
                .appendImpl("    end;")
                .appendImpl("    else")
                .appendImpl("      Log.Debug('Unexpected exception!');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testElseWithMultipleStatementsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new SwallowedExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    ThrowException;")
                .appendImpl("  except")
                .appendImpl("    on E: MyException do begin")
                .appendImpl("      Log.Debug('exception handler');")
                .appendImpl("    end;")
                .appendImpl("    else")
                .appendImpl("      Log.Debug('Unexpected exception!');")
                .appendImpl("      Cleanup;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testEmptyExceptBlockShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new SwallowedExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    ThrowException;")
                .appendImpl("  except")
                .appendImpl("    // Do nothing")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(11);
  }

  @Test
  void testEmptyHandlerShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new SwallowedExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    ThrowException;")
                .appendImpl("  except")
                .appendImpl("    on E: MyException do begin")
                .appendImpl("      // Do nothing")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(12);
  }

  @Test
  void testEmptyHandlerWithoutBeginShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new SwallowedExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    ThrowException;")
                .appendImpl("  except")
                .appendImpl("    on E: MyException do;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(12);
  }
}
