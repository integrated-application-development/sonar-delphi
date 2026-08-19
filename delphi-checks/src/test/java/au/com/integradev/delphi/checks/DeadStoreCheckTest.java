/*
 * Sonar Delphi Plugin
 * Copyright (C) 2026 Integrated Application Development
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

import au.com.integradev.delphi.builders.DelphiTestProgramBuilder;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class DeadStoreCheckTest {
  private static final String END_BLOCK = "if False then;";

  @Test
  void testRepeatedAssignmentsInSameBlockShouldAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var A := 10; // Noncompliant")
            .appendImpl("  A := 11; // Noncompliant")
            .appendImpl("  A := 12;")
            .appendImpl("  Writeln(A);")
            .appendImpl("end;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @Test
  void testRepeatedAssignmentsInDifferentBlockShouldAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var A := 10; // Noncompliant")
            .appendImpl("  A := 11; // Noncompliant")
            .appendImpl(END_BLOCK)
            .appendImpl("  A := 12;")
            .appendImpl("  Writeln(A);")
            .appendImpl("end;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @Test
  void testDummyForLoopVarDeclarationShouldNotAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var A := 10;")
            .appendImpl("  for var I := 1 to 10 do")
            .appendImpl("    Writeln(A);")
            .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @Test
  void testDummyForLoopVarReferenceShouldNotAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var A := 10;")
            .appendImpl("  var I;")
            .appendImpl("  for I := 1 to 10 do")
            .appendImpl("    Writeln(A);")
            .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @Test
  void testOverwriteWithForLoopVarShouldAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var I := 10; // Noncompliant")
            .appendImpl("  for I := 1 to 10 do")
            .appendImpl("    Writeln(1);")
            .appendImpl("end;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @Test
  void testForLoopVarReassignmentShouldAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  for var I := 1 to 10 do begin")
            .appendImpl("    I := 10; // Noncompliant")
            .appendImpl("  end;")
            .appendImpl("end;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @Test
  void testExceptionHandlerShouldNotAddIssues() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("  except")
            .appendImpl("    on E: Exception do raise;")
            .appendImpl("    on F: Exception do;")
            .appendImpl("  end;")
            .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  private static Stream<Arguments> provideExceptedValues() {
    return Stream.of(
        Arguments.of("-1"),
        Arguments.of("-  1"),
        Arguments.of("0"),
        Arguments.of("1"),
        Arguments.of("+  1"),
        Arguments.of("True"),
        Arguments.of("tRuE"),
        Arguments.of("False"),
        Arguments.of("fAlSe"),
        Arguments.of("nil"),
        Arguments.of("NiL"),
        Arguments.of("[]"),
        Arguments.of("[ ]"),
        Arguments.of("''"));
  }

  @ParameterizedTest
  @MethodSource("provideExceptedValues")
  void testLocalRedefinedExceptionShouldAddIssues(String value) {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var A := " + value + "; // Noncompliant")
            .appendImpl("  A := 10;")
            .appendImpl("  Writeln(A);")
            .appendImpl("end;");
    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @ParameterizedTest
  @MethodSource("provideExceptedValues")
  void testReferenceLocalReassignmentExceptionShouldAddIssues(String value) {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("  var A: Integer;")
            .appendImpl("begin")
            .appendImpl("  A := " + value + "; // Noncompliant")
            .appendImpl("  A := 10;")
            .appendImpl("  Writeln(A);")
            .appendImpl("end;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @ParameterizedTest
  @MethodSource("provideExceptedValues")
  void testLocalReassignedExceptionShouldNotAddIssues(String value) {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var A := " + value + ";")
            .appendImpl(END_BLOCK)
            .appendImpl("  A := 10;")
            .appendImpl("  Writeln(A);")
            .appendImpl("end;");
    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @ParameterizedTest
  @MethodSource("provideExceptedValues")
  void testReferenceReassignedExceptionShouldNotAddIssues(String value) {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var A: Integer;")
            .appendImpl("begin")
            .appendImpl("  A := " + value + ";")
            .appendImpl(END_BLOCK)
            .appendImpl("  A := 10;")
            .appendImpl("  Writeln(A);")
            .appendImpl("end;");
    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @ParameterizedTest
  @MethodSource("provideExceptedValues")
  void testExceptedSingleAssignmentShouldAddIssue(String value) {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var A := " + value + "; // Noncompliant")
            .appendImpl("end;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @Test
  void testSingleAssignmentShouldAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var A := 10; // Noncompliant")
            .appendImpl("end;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @Test
  void testRepeatedExceptionAssignmentShouldAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var A := -1; // Noncompliant")
            .appendImpl("  A := 0; // Noncompliant")
            .appendImpl("  A := 1;")
            .appendImpl("  Writeln(A);")
            .appendImpl("end;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @Test
  void testRepeatedExceptionAcrossBlockAssignmentShouldAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var A := -1; // Noncompliant")
            .appendImpl("  A := 0;")
            .appendImpl(END_BLOCK)
            .appendImpl("  A := 1; // Noncompliant")
            .appendImpl("  A := 0;")
            .appendImpl("  Writeln(A);")
            .appendImpl("end;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @Test
  void testRepeatedMultiVariableAssignmentShouldAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var A := 1; // Noncompliant")
            .appendImpl("  A := 5; // Noncompliant")
            .appendImpl("  A := 10;")
            .appendImpl("  var B := A; // Noncompliant")
            .appendImpl("  A := 4; // Noncompliant")
            .appendImpl("  A := 9;")
            .appendImpl("  B := 9;")
            .appendImpl("  Writeln(A);")
            .appendImpl("  Writeln(B);")
            .appendImpl("end;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  private static DelphiTestUnitBuilder getAllDeclarationsUnit(String... statements) {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendDecl("var IntGlobal: Integer;")
            .appendImpl("type")
            .appendImpl("  TTest = class")
            .appendImpl("    class var ClassVar: Integer;")
            .appendImpl("    var Field: Integer;")
            .appendImpl("    var ArrayField: array[0..1] of Integer;")
            .appendImpl("    procedure SetField(Val: Integer);")
            .appendImpl("    function GetField: Integer;")
            .appendImpl("    property VVProp: Integer read Field write Field;")
            .appendImpl("    property VMProp: Integer read Field write SetField;")
            .appendImpl("    property MVProp: Integer read GetField write Field;")
            .appendImpl("    property MMProp: Integer read GetField write SetField;")
            .appendImpl("    function Test(")
            .appendImpl("      Param: Integer;")
            .appendImpl("      var VarParam: Integer;")
            .appendImpl("      out OutParam: Integer;")
            .appendImpl("      PointerParam: PInteger")
            .appendImpl("    ): Integer;")
            .appendImpl("  end;")
            .appendImpl("var ImplGlobal: Integer;")
            .appendImpl("function TTest.Test(")
            .appendImpl("  Param: Integer;")
            .appendImpl("  var VarParam: Integer;")
            .appendImpl("  out OutParam: Integer;")
            .appendImpl("  PointerParam: PInteger")
            .appendImpl("): Integer;")
            .appendImpl("var")
            .appendImpl("  Local: Integer;")
            .appendImpl("begin")
            .appendImpl("  var InlineVar: Integer;");
    for (String statement : statements) {

      unitBuilder.appendImpl("  " + statement);
    }
    return unitBuilder.appendImpl("end;");
  }

  private static Stream<Arguments> provideExternallyAccessibleVariables() {
    return Stream.of(
        Arguments.of("IntGlobal"),
        Arguments.of("ClassVar"),
        Arguments.of("Field"),
        Arguments.of("ArrayField"),
        Arguments.of("VVProp"),
        Arguments.of("ImplGlobal"),
        Arguments.of("Result"),
        Arguments.of("Test"),
        Arguments.of("VarParam"),
        Arguments.of("OutParam"),
        Arguments.of("PointerParam"));
  }

  private static Stream<Arguments> provideInternallyAccessibleVariables() {
    return Stream.of(Arguments.of("Param"), Arguments.of("Local"), Arguments.of("InlineVar"));
  }

  @ParameterizedTest
  @MethodSource("provideExternallyAccessibleVariables")
  void testRepeatedExternalValuesInSameBlockShouldAddIssue(String variable) {
    DelphiTestUnitBuilder unit =
        getAllDeclarationsUnit(variable + " := 1; // Noncompliant", variable + " := 2;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unit).verifyIssues();
  }

  @ParameterizedTest
  @MethodSource("provideExternallyAccessibleVariables")
  void testRepeatedExternalValuesInDifferentBlocksShouldAddIssue(String variable) {
    DelphiTestUnitBuilder unit =
        getAllDeclarationsUnit(variable + " := 2; // Noncompliant", END_BLOCK, variable + " := 2;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unit).verifyIssues();
  }

  @ParameterizedTest
  @MethodSource("provideExternallyAccessibleVariables")
  void testExternalValuesShouldNotAddIssue(String variable) {
    DelphiTestUnitBuilder unit = getAllDeclarationsUnit(variable + " := 2;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unit).verifyNoIssues();
  }

  @ParameterizedTest
  @MethodSource("provideExternallyAccessibleVariables")
  void testExternalValuesAndRaiseShouldNotAddIssue(String variable) {
    DelphiTestUnitBuilder unit = getAllDeclarationsUnit(variable + " := 2;", "raise;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unit).verifyNoIssues();
  }

  @ParameterizedTest
  @MethodSource("provideInternallyAccessibleVariables")
  void testRepeatedInternalValuesInSameBlockShouldAddIssue(String variable) {
    DelphiTestUnitBuilder unit =
        getAllDeclarationsUnit(
            variable + " := 1; // Noncompliant", variable + " := 2; // Noncompliant");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unit).verifyIssues();
  }

  @ParameterizedTest
  @MethodSource("provideInternallyAccessibleVariables")
  void testRepeatedInternalValuesInDifferentBlocksShouldAddIssue(String variable) {
    DelphiTestUnitBuilder unit =
        getAllDeclarationsUnit(
            variable + " := 2; // Noncompliant", END_BLOCK, variable + " := 2; // Noncompliant");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unit).verifyIssues();
  }

  @ParameterizedTest
  @MethodSource("provideInternallyAccessibleVariables")
  void testInternalValuesShouldAddIssue(String variable) {
    DelphiTestUnitBuilder unit = getAllDeclarationsUnit(variable + " := 2; // Noncompliant");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unit).verifyIssues();
  }

  @ParameterizedTest
  @MethodSource("provideInternallyAccessibleVariables")
  void testInternalValuesAndRaiseShouldAddIssue(String variable) {
    DelphiTestUnitBuilder unit =
        getAllDeclarationsUnit(variable + " := 2; // Noncompliant", "raise;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unit).verifyIssues();
  }

  @ParameterizedTest
  @MethodSource("provideInternallyAccessibleVariables")
  void testInternalValuesAfterMethodCallShouldAddIssue(String variable) {
    DelphiTestUnitBuilder unit =
        getAllDeclarationsUnit(
            variable + " := 2;", "Writeln(" + variable + ");", variable + " := 3; // Noncompliant");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unit).verifyIssues();
  }

  @Test
  void testVariableOnlyPropertyAndFieldShouldAddIssues() {
    DelphiTestUnitBuilder unit =
        getAllDeclarationsUnit("VVProp := 1; // Noncompliant", "Field := 2;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unit).verifyIssues();
  }

  @Test
  void testFieldAndVariableOnlyPropertyShouldAddIssues() {
    DelphiTestUnitBuilder unit =
        getAllDeclarationsUnit("Field := 1; // Noncompliant", "VVProp := 2;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unit).verifyIssues();
  }

  private static Stream<Arguments> provideNonVariableOnlyProperties() {
    return Stream.of(Arguments.of("VMProp"), Arguments.of("MVProp"), Arguments.of("MMProp"));
  }

  @ParameterizedTest
  @MethodSource("provideNonVariableOnlyProperties")
  void testNonVariableOnlyPropertyAndFieldShouldNotAddIssues(String propName) {
    DelphiTestUnitBuilder unit = getAllDeclarationsUnit(propName + " := 1;", "Field := 2;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unit).verifyNoIssues();
  }

  @ParameterizedTest
  @MethodSource("provideNonVariableOnlyProperties")
  void testFieldAndNonVariableOnlyPropertyShouldNotAddIssues(String propName) {
    DelphiTestUnitBuilder unit = getAllDeclarationsUnit("Field := 1;", propName + " := 2;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unit).verifyNoIssues();
  }

  @Test
  void testArrayPropertiesShouldNotAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendDecl("var IntGlobal: Integer;")
            .appendImpl("type")
            .appendImpl("  TTest = class")
            .appendImpl("    var ArrayField: array[0..1] of Integer;")
            .appendImpl("    property Prop1: Integer read ArrayField[0] write ArrayField[0];")
            .appendImpl("    property Prop2: Integer read ArrayField[1] write ArrayField[1];")
            .appendImpl("    procedure Test;")
            .appendImpl("  end;")
            .appendImpl("procedure TTest.Test;")
            .appendImpl("begin")
            .appendImpl("  Prop1 := 4;")
            .appendImpl("  Prop1 := 5;")
            .appendImpl("  Prop2 := 6;")
            .appendImpl("  Prop2 := 7;")
            .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @Test
  void testParentClassFieldAssignmentThenMethodCallShouldNotAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("type")
            .appendImpl("  TParent = class")
            .appendImpl("    var ParentA: Integer;")
            .appendImpl("  end;")
            .appendImpl("  TTest = class(TParent)")
            .appendImpl("    var A: Integer;")
            .appendImpl("    procedure Test;")
            .appendImpl("  end;")
            .appendImpl("procedure TTest.Test;")
            .appendImpl("begin")
            .appendImpl("  A := 12;")
            .appendImpl("  Test;")
            .appendImpl("  A := 13;")
            .appendImpl("  ParentA := 12;")
            .appendImpl("  Test;")
            .appendImpl("  ParentA := 13;")
            .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @Test
  void testArrayAssignmentShouldNotAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var Arr: array[0..1] of Integer;")
            .appendImpl("begin")
            .appendImpl("  Arr[0] := 10;")
            .appendImpl("  Arr[1] := 10;")
            .appendImpl("  Writeln(Arr);")
            .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @Test
  void failOnUpgrade_testRepeatedIndexArrayAssignmentShouldNotAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var Arr: array[0..1] of Integer;")
            .appendImpl("begin")
            .appendImpl("  Arr[0] := 10;")
            .appendImpl("  Arr[0] := 10;")
            .appendImpl("  Writeln(Arr);")
            .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @Test
  void testConstructorSelfAssignmentShouldNotAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("type")
            .appendImpl("  TTest = record")
            .appendImpl("    var A: Integer;")
            .appendImpl("    constructor Create(A: Integer);")
            .appendImpl("  end;")
            .appendImpl("constructor Create(A: Integer);")
            .appendImpl("begin")
            .appendImpl("  Self := Default(TTest);")
            .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @Test
  void testConstructorRepeatedSelfAssignmentShouldNotAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("type")
            .appendImpl("  TTest = record")
            .appendImpl("    var A: Integer;")
            .appendImpl("    constructor Create(A: Integer);")
            .appendImpl("  end;")
            .appendImpl("constructor Create(A: Integer);")
            .appendImpl("begin")
            .appendImpl("  Self := Default(TTest); // Noncompliant")
            .appendImpl("  Self := Default(TTest);")
            .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @Test
  void testUseInAnonymousRoutineShouldNotAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure DoProc(proc: TProc); forward;")
            .appendImpl("procedure Consume(val: Integer); forward;")
            .appendImpl("procedure TTest.Test;")
            .appendImpl("begin")
            .appendImpl("  var A := 10;")
            .appendImpl("  DoProc(procedure begin Consume(A); end);")
            .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @Test
  void testAssignInAnonymousRoutineShouldNotAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure DoProc(proc: TProc); forward;")
            .appendImpl("procedure Consume(val: Integer); forward;")
            .appendImpl("procedure TTest.Test;")
            .appendImpl("var A: Integer;")
            .appendImpl("begin")
            .appendImpl("  DoProc(procedure begin A := 10; end);")
            .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @Test
  void testAssignmentAfterUseShouldAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var A := 1;")
            .appendImpl("  Writeln(A);")
            .appendImpl("  A := 6; // Noncompliant")
            .appendImpl("end;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @Test
  void testUnusedBranchAssignmentsShouldAddIssues() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var A;")
            .appendImpl("  if A then")
            .appendImpl("    A := 10 // Noncompliant")
            .appendImpl("  else")
            .appendImpl("    A := 11; // Noncompliant")
            .appendImpl("end;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @Test
  void testDefaultThenBranchAssignmentsShouldNotAddIssues() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var A: String;")
            .appendImpl("begin")
            .appendImpl("  A := '';")
            .appendImpl("  if A then")
            .appendImpl("    A := 'foo'")
            .appendImpl("  else")
            .appendImpl("    A := 'bar';")
            .appendImpl("  Writeln(A);")
            .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @Test
  void testResultConditionalEarlyExitShouldNotAddIssues() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Test: Integer;")
            .appendImpl("begin")
            .appendImpl("  Result := 11;")
            .appendImpl("  if True then Exit;")
            .appendImpl("  Result := 10;")
            .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @Test
  void testRoutineVarWithSubroutineShouldAddIssues() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Test: Integer;")
            .appendImpl("var BeforeSubRoutineVar: Integer;")
            .appendImpl("  procedure TestSub;")
            .appendImpl("  var InSubRoutineVar: Integer;")
            .appendImpl("  begin")
            .appendImpl("    Writeln(BeforeSubRoutineVar);")
            .appendImpl("    InSubRoutineVar := 11; // Noncompliant")
            .appendImpl("  end;")
            .appendImpl("var AfterSubRoutineVar: Integer;")
            .appendImpl("begin")
            .appendImpl("  BeforeSubRoutineVar := 11;")
            .appendImpl("  AfterSubRoutineVar := 11; // Noncompliant")
            .appendImpl("  TestSub;")
            .appendImpl("end;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @Test
  void testNonSubprocedureUsedVariablesShouldAddIssues() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var Before: Integer;")
            .appendImpl("  procedure SubProc; begin Writeln(Before); end;")
            .appendImpl("var After: Integer;")
            .appendImpl("begin")
            .appendImpl("  Before := 10;")
            .appendImpl("  After := 10; // Noncompliant")
            .appendImpl("  SubProc;")
            .appendImpl("  Before := 11;")
            .appendImpl("  After := 11;")
            .appendImpl("  Writeln(Before);")
            .appendImpl("  Writeln(After);")
            .appendImpl("end;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @Test
  void testNonSubprocedureNonUsedVariablesShouldAddIssues() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var Before: Integer;")
            .appendImpl("  procedure SubProc; begin end;")
            .appendImpl("var After: Integer;")
            .appendImpl("begin")
            .appendImpl("  Before := 10; // Noncompliant")
            .appendImpl("  After := 10; // Noncompliant")
            .appendImpl("  SubProc;")
            .appendImpl("  Before := 11;")
            .appendImpl("  After := 11;")
            .appendImpl("  Writeln(Before);")
            .appendImpl("  Writeln(After);")
            .appendImpl("end;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @Test
  void testConditionalAssignSubprocedureShouldNotAddIssues() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Test: Boolean;")
            .appendImpl("var Local: Integer;")
            .appendImpl("  function Check: Boolean; begin Result := Local = 1; end;")
            .appendImpl("begin")
            .appendImpl("  Local := 0;")
            .appendImpl("  if True then")
            .appendImpl("    Local := 10;")
            .appendImpl("  Check;")
            .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @Test
  void testExternalProcAndParamShouldAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure External; begin end;")
            .appendImpl("procedure Test(Param: Integer);")
            .appendImpl("begin")
            .appendImpl("  Param := 2; // Noncompliant")
            .appendImpl("  External;")
            .appendImpl("end;");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @Test
  void testVariableAndSubprocedureNameCollisionShouldNotAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  CharSet = set of Char;")
            .appendImpl("procedure Test;")
            .appendImpl("  procedure SubProc(Chars: CharSet); begin end;")
            .appendImpl("var Chars: CharSet;")
            .appendImpl("begin")
            .appendImpl("  Chars := [];")
            .appendImpl("  Assert(Chars = []);")
            .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @Test
  void testBinaryExpressionUseOfLocalVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl(
                "  function Proc(const Local: string; out Obj: TObject): Boolean; begin end;")
            .appendImpl("function Test: TObject;")
            .appendImpl("var Local: string;")
            .appendImpl("var Found: Boolean;")
            .appendImpl("begin")
            .appendImpl("  Local := '123';")
            .appendImpl("  Found := Proc(Local, Result) and Proc(Local, Result);")
            .appendImpl("  Writeln(Found);")
            .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @Test
  void testBinaryExpressionSubProcUseOfLocalVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Test: TObject;")
            .appendImpl(
                "  function SubProc(const Local: string; out Obj: TObject): Boolean; begin end;")
            .appendImpl("var Local: string;")
            .appendImpl("var Found: Boolean;")
            .appendImpl("begin")
            .appendImpl("  Local := '123';")
            .appendImpl("  Found := SubProc(Local, Result) and SubProc(Local, Result);")
            .appendImpl("  Writeln(Found);")
            .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "OnDo;",
        "OnDoObj(Self);",
      })
  void testInvocableAfterFieldShouldNotAddIssue(String statement) {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("type")
            .appendImpl("  TEvent = reference to procedure;")
            .appendImpl("  TObjectEvent = reference to procedure(Obj: TObject);")
            .appendImpl("  TTest = class")
            .appendImpl("  var")
            .appendImpl("    CanDo: Boolean;")
            .appendImpl("    OnDo: TEvent;")
            .appendImpl("    OnDoObj: TObjectEvent;")
            .appendImpl("    procedure Test;")
            .appendImpl("  end;")
            .appendImpl("procedure TTest.Test;")
            .appendImpl("begin")
            .appendImpl("  CanDo := True;")
            .appendImpl("  " + statement)
            .appendImpl("  CanDo := False;")
            .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new DeadStoreCheck())
        .onFile(unitBuilder)
        .verifyNoIssues();
  }

  @Test
  void testInitializationShouldAddIssues() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("initialization")
            .appendImpl("  var Local := '123'; // Noncompliant")
            .appendImpl("  WriteLn();")
            .appendImpl("  Local := '12'; // Noncompliant");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @Test
  void testFinalizationShouldAddIssues() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("initialization")
            .appendImpl("finalization")
            .appendImpl("  var Local := '123'; // Noncompliant")
            .appendImpl("  WriteLn();")
            .appendImpl("  Local := '12'; // Noncompliant");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @Test
  void testUnitBeginShouldAddIssues() {
    DelphiTestUnitBuilder unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("begin")
            .appendImpl("  var Local := '123'; // Noncompliant")
            .appendImpl("  WriteLn();")
            .appendImpl("  Local := '12'; // Noncompliant");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }

  @Test
  void testProgramShouldAddIssues() {
    DelphiTestProgramBuilder unitBuilder =
        new DelphiTestProgramBuilder()
            .appendImpl("  var Local := '123'; // Noncompliant")
            .appendImpl("  WriteLn();")
            .appendImpl("  Local := '12'; // Noncompliant");

    CheckVerifier.newVerifier().withCheck(new DeadStoreCheck()).onFile(unitBuilder).verifyIssues();
  }
}
