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
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import org.junit.jupiter.api.Test;

class ReRaiseExceptionCheckTest extends CheckTest {

  @Test
  void testRaiseInExceptShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    raise;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ReRaiseExceptionRule"));
  }

  @Test
  void testRaiseInExceptionHandlerShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
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
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ReRaiseExceptionRule"));
  }

  @Test
  void testRaisingNormalExceptionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure ThrowException;")
            .appendImpl("begin")
            .appendImpl("  raise Exception.Create('Spooky scary skeletons!');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("ReRaiseExceptionRule", builder.getOffset() + 3));
  }

  @Test
  void testRaiseInExceptionHandlerWithNoSemicolonOrBeginEndShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on E: Exception do raise")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("ReRaiseExceptionRule", builder.getOffset() + 6));
  }

  @Test
  void testBadRaiseShouldAddIssue() {
    DelphiTestUnitBuilder builder =
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
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("ReRaiseExceptionRule", builder.getOffset() + 7));
  }

  @Test
  void testBadRaiseWithNoSemicolonOrBeginEndShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on E: Exception do raise E")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("ReRaiseExceptionRule", builder.getOffset() + 6));
  }

  @Test
  void testMultipleBadRaisesShouldAddIssues() {
    DelphiTestUnitBuilder builder =
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
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ReRaiseExceptionRule", builder.getOffset() + 8))
        .areExactly(1, ruleKeyAtLine("ReRaiseExceptionRule", builder.getOffset() + 10));
  }

  @Test
  void testRaiseDifferentExceptionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
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
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ReRaiseExceptionRule"));
  }

  @Test
  void testRaiseDifferentExceptionWithoutIdentifierShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
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
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("ReRaiseExceptionRule", builder.getOffset() + 7));
  }
}
