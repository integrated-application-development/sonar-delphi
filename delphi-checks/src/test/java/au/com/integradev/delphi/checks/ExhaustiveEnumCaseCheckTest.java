/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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

class ExhaustiveEnumCaseCheckTest {
  @Test
  void testNonEnumCaseShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExhaustiveEnumCaseCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test(MyVal: Integer);")
                .appendImpl("begin")
                .appendImpl("  case MyVal of")
                .appendImpl("    1: Writeln('one');")
                .appendImpl("    2: Writeln('two');")
                .appendImpl("    3: Writeln('three');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testExhaustiveEnumShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExhaustiveEnumCaseCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyEnum = (meOne, meTwo, meThree);")
                .appendImpl("procedure Test(MyVal: TMyEnum);")
                .appendImpl("begin")
                .appendImpl("  case MyVal of")
                .appendImpl("    meOne: Writeln('one');")
                .appendImpl("    meTwo: Writeln('two');")
                .appendImpl("    meThree: Writeln('three');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testExhaustiveEnumMultipleElementsPerArmShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExhaustiveEnumCaseCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyEnum = (meOne, meTwo, meThree);")
                .appendImpl("procedure Test(MyVal: TMyEnum);")
                .appendImpl("begin")
                .appendImpl("  case MyVal of")
                .appendImpl("    meOne, meTwo: Writeln('one');")
                .appendImpl("    meThree: Writeln('three');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNonEnumElementsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExhaustiveEnumCaseCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyEnum = (meOne, meTwo, meThree);")
                .appendImpl("procedure Test(MyVal: TMyEnum);")
                .appendImpl("begin")
                .appendImpl("  case MyVal of")
                .appendImpl("    meOne, meTwo, 5: Writeln('one');")
                .appendImpl("    meThree, Integer.MaxValue: Writeln('three');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNonExhaustiveEnumShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExhaustiveEnumCaseCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyEnum = (meOne, meTwo, meIgnored, meAlsoIgnored);")
                .appendImpl("procedure Test(MyVal: TMyEnum);")
                .appendImpl("begin")
                .appendImpl("  case MyVal of // Noncompliant")
                .appendImpl("    meOne: Writeln('one');")
                .appendImpl("    meTwo: Writeln('two');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testExhaustiveSubrangeExpressionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExhaustiveEnumCaseCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyEnum = (meFirst, meIncluded, meLast, meIgnored, meAlsoIgnored);")
                .appendImpl("procedure Test(MyVal: TMyEnum);")
                .appendImpl("begin")
                .appendImpl("  case MyVal of")
                .appendImpl("    meFirst..meLast: Writeln('foo');")
                .appendImpl("    meIgnored, meAlsoIgnored: Writeln('bar');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNonExhaustiveSubrangeExpressionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExhaustiveEnumCaseCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyEnum = (meFirst, meIncluded, meLast, meIgnored, meAlsoIgnored);")
                .appendImpl("procedure Test(MyVal: TMyEnum);")
                .appendImpl("begin")
                .appendImpl("  case MyVal of")
                .appendImpl("    meFirst..meLast: Writeln('foo');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testFunctionCallSelectorExpressionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExhaustiveEnumCaseCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyEnum = (meOne, meTwo, meIgnored, meAlsoIgnored);")
                .appendDecl("  TMyObject = class(TObject)")
                .appendDecl("    function GetMyVal: TMyEnum;")
                .appendDecl("  end;")
                .appendImpl("procedure Test(MyObj: TMyObject);")
                .appendImpl("begin")
                .appendImpl("  case MyObj.GetMyVal of // Noncompliant")
                .appendImpl("    meOne: Writeln('one');")
                .appendImpl("    meTwo: Writeln('two');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNotInvokedFunctionCallSelectorExpressionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExhaustiveEnumCaseCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyEnum = (meOne, meTwo, meIgnored, meAlsoIgnored);")
                .appendDecl("  TMyObject = class(TObject)")
                .appendDecl("    function GetMyVal(Arg: string): TMyEnum;")
                .appendDecl("  end;")
                .appendImpl("procedure Test(MyObj: TMyObject);")
                .appendImpl("begin")
                .appendImpl("  case MyObj.GetMyVal of")
                .appendImpl("    meOne: Writeln('one');")
                .appendImpl("    meTwo: Writeln('two');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testPropertySelectorExpressionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExhaustiveEnumCaseCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyEnum = (meOne, meTwo, meIgnored, meAlsoIgnored);")
                .appendDecl("  TMyObject = class(TObject)")
                .appendDecl("    FVal: TMyEnum;")
                .appendDecl("    property MyVal: TMyEnum read FVal write FVal;")
                .appendDecl("  end;")
                .appendImpl("procedure Test(MyObj: TMyObject);")
                .appendImpl("begin")
                .appendImpl("  case MyObj.MyVal of // Noncompliant")
                .appendImpl("    meOne: Writeln('one');")
                .appendImpl("    meTwo: Writeln('two');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNonExhaustiveEnumMultipleElementsPerArmShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExhaustiveEnumCaseCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyEnum = (meOne, meTwo, meIgnored, meAlsoIgnored);")
                .appendImpl("procedure Test(MyVal: TMyEnum);")
                .appendImpl("begin")
                .appendImpl("  case MyVal of // Noncompliant")
                .appendImpl("    meOne, meTwo: Writeln('one or two');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testCaseWithDefaultShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExhaustiveEnumCaseCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyEnum = (meOne, meTwo, meThree);")
                .appendImpl("procedure Test(MyVal: TMyEnum);")
                .appendImpl("begin")
                .appendImpl("  case MyVal of")
                .appendImpl("    meOne: Writeln('one');")
                .appendImpl("  else")
                .appendImpl("    Writeln('anything else');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
