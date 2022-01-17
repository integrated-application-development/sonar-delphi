package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class VariableInitializationRuleTest extends BasePmdRuleTest {
  @Test
  void testRecordCreateShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    System.SysUtils")
            .appendDecl("  ;")
            .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  FormatSettings: TFormatSettings;")
            .appendImpl("begin")
            .appendImpl("  FormatSettings := TFormatSettings.Create;")
            .appendImpl("  Foo(FormatSettings);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testVariableAssignedFromExternalVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    System.SysUtils")
            .appendDecl("  ;")
            .appendDecl("var")
            .appendDecl("  MyFormatSettings: TFormatSettings;")
            .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  FormatSettings: TFormatSettings;")
            .appendImpl("begin")
            .appendImpl("  FormatSettings := MyFormatSettings;")
            .appendImpl("  Foo(FormatSettings);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testVariableAssignedFromLocalInitializedVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    System.SysUtils")
            .appendDecl("  ;")
            .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  FormatSettings1: TFormatSettings;")
            .appendImpl("  FormatSettings2: TFormatSettings;")
            .appendImpl("begin")
            .appendImpl("  FormatSettings1 := TFormatSettings.Create;")
            .appendImpl("  FormatSettings2 := FormatSettings1;")
            .appendImpl("  Foo(FormatSettings2);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testVariableAssignedFromLocalUninitializedVariableShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Foo(Int: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Int1: Integer;")
            .appendImpl("  Int2: Integer;")
            .appendImpl("begin")
            .appendImpl("  Int2 := Int1;")
            .appendImpl("  Foo(Int2);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 7));
  }

  @Test
  void testInlineVariableAssignedFromLocalInitializedVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    System.SysUtils")
            .appendDecl("  ;")
            .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var FormatSettings1 := TFormatSettings.Create;")
            .appendImpl("  var FormatSettings2 := FormatSettings1;")
            .appendImpl("  Foo(FormatSettings2);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testInlineVariableAssignedFromLocalUninitializedVariableShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    System.SysUtils")
            .appendDecl("  ;")
            .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var FormatSettings1: TFormatSettings;")
            .appendImpl("  var FormatSettings2 := FormatSettings1;")
            .appendImpl("  Foo(FormatSettings2);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 4))
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 5));
  }

  @Test
  void testInlineConstAssignedFromLocalInitializedVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    System.SysUtils")
            .appendDecl("  ;")
            .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var FormatSettings1 := TFormatSettings.Create;")
            .appendImpl("  const FormatSettings2 = FormatSettings1;")
            .appendImpl("  Foo(FormatSettings2);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testInlineConstAssignedFromLocalUninitializedVariableShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    System.SysUtils")
            .appendDecl("  ;")
            .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var FormatSettings1: TFormatSettings;")
            .appendImpl("  const FormatSettings2 = FormatSettings1;")
            .appendImpl("  Foo(FormatSettings2);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 4))
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 5));
  }

  @Test
  void testInlineRecordCreateShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    System.SysUtils")
            .appendDecl("  ;")
            .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var FormatSettings: TFormatSettings;")
            .appendImpl("  FormatSettings := TFormatSettings.Create;")
            .appendImpl("  Foo(FormatSettings);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testInlineRecordCreateWithImmediateInitializationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    System.SysUtils")
            .appendDecl("  ;")
            .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var FormatSettings := TFormatSettings.Create;")
            .appendImpl("  const FormatSettings2 = TFormatSettings.Create;")
            .appendImpl("  Foo(FormatSettings);")
            .appendImpl("  Foo(FormatSettings2);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testNestedRecordCreateShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    System.SysUtils")
            .appendDecl("  ;")
            .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  FormatSettings: TFormatSettings;")
            .appendImpl("begin")
            .appendImpl("  if True then begin")
            .appendImpl("    if True then begin")
            .appendImpl("      FormatSettings := TFormatSettings.Create;")
            .appendImpl("    end;")
            .appendImpl("    Foo(FormatSettings);")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testUnusedVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("uses")
            .appendImpl("    System.SysUtils")
            .appendImpl("  ;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  FormatSettings: TFormatSettings;")
            .appendImpl("begin")
            .appendImpl("  WriteLn('Foo');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testVariableWithoutInitializationShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    System.SysUtils")
            .appendDecl("  ;")
            .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  FormatSettings: TFormatSettings;")
            .appendImpl("begin")
            .appendImpl("  Foo(FormatSettings);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 5));
  }

  @Test
  void testVariableWithoutInitializationShouldOnlyAddOneIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    System.SysUtils")
            .appendDecl("  ;")
            .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  FormatSettings: TFormatSettings;")
            .appendImpl("begin")
            .appendImpl("  Foo(FormatSettings);")
            .appendImpl("  Foo(FormatSettings);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 6));
  }

  @Test
  void testInlineVariableWithoutInitializationShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    System.SysUtils")
            .appendDecl("  ;")
            .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var FormatSettings: TFormatSettings;")
            .appendImpl("  Foo(FormatSettings);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 4));
  }

  @Test
  void testMultipleInlineVariablesWithoutInitializationShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    System.SysUtils")
            .appendDecl("  ;")
            .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var FormatSettings, FormatSettings2, FormatSettings3: TFormatSettings;")
            .appendImpl("  Foo(FormatSettings);")
            .appendImpl("  Foo(FormatSettings2);")
            .appendImpl("  Foo(FormatSettings3);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 4))
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 5))
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 6));
  }

  @Test
  void testRecordWithPartialInitializationShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBar = record")
            .appendDecl("    Int: Integer;")
            .appendDecl("    I64: Int64;")
            .appendDecl("  end;")
            .appendDecl("procedure Foo(Bar: TBar);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Bar.Int := 123;")
            .appendImpl("  Foo(Bar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 6));
  }

  @Test
  void testRecordWithInitializationOfAllUnmanagedFieldsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBar = record")
            .appendDecl("    Int: Integer;")
            .appendDecl("    Str: String;")
            .appendDecl("  end;")
            .appendDecl("procedure Foo(Bar: TBar);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Bar.Int := 123;")
            .appendImpl("  Foo(Bar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testUninitializedVariantRecordShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBar = record")
            .appendDecl("    Int: Integer;")
            .appendDecl("    case Boolean of")
            .appendDecl("      True: (Int2: Integer);")
            .appendDecl("      False: (Int3: Integer);")
            .appendDecl("  end;")
            .appendDecl("procedure Foo(Bar: TBar);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Foo(Bar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 5));
  }

  @Test
  void testPartiallyInitializedVariantRecordShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBar = record")
            .appendDecl("    Int: Integer;")
            .appendDecl("    case Boolean of")
            .appendDecl("      True: (Int2: Integer);")
            .appendDecl("      False: (Int3: Integer);")
            .appendDecl("  end;")
            .appendDecl("procedure Foo(Bar: TBar);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Bar.Int := 123;")
            .appendImpl("  Foo(Bar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testUninitializedVariantRecordWithOnlyVariantFieldsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBar = record")
            .appendDecl("    case Boolean of")
            .appendDecl("      True: (Int2: Integer);")
            .appendDecl("      False: (Int3: Integer);")
            .appendDecl("  end;")
            .appendDecl("procedure Foo(Bar: TBar);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Foo(Bar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testRecordFieldAssignedLocalVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBar = record")
            .appendDecl("    Int: Integer;")
            .appendDecl("    Str: String;")
            .appendDecl("  end;")
            .appendDecl("procedure Foo(Bar: TBar);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Int: Integer;")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Int := 123;")
            .appendImpl("  Bar.Int := Int;")
            .appendImpl("  Foo(Bar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testRecordFieldAssignedUninitializedLocalVariableShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBar = record")
            .appendDecl("    Int: Integer;")
            .appendDecl("    Str: String;")
            .appendDecl("  end;")
            .appendDecl("procedure Foo(Int: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Int: Integer;")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Bar.Int := Int;")
            .appendImpl("  Foo(Bar.Int);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 7));
  }

  @Test
  void testRecordWithManagedFieldsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBar = record")
            .appendDecl("    I: IInterface;")
            .appendDecl("    S: String;")
            .appendDecl("    A: array of String;")
            .appendDecl("    V: Variant;")
            .appendDecl("  end;")
            .appendDecl("procedure Foo(Bar: TBar);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Foo(Bar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testUninitializedRecordFieldAssignedToVariableShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBar = record")
            .appendDecl("    Int: Integer;")
            .appendDecl("  end;")
            .appendDecl("procedure Foo(Int: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("  Int: Integer;")
            .appendImpl("begin")
            .appendImpl("  Int := Bar.Int;")
            .appendImpl("  Foo(Int);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areNot(ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 5))
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 6));
  }

  @Test
  void testPointerToUninitializedRecordShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  PFormatSettings = ^TFormatSettings;")
            .appendDecl("procedure Foo(FormatSettings: PFormatSettings);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  FormatSettings: TFormatSettings;")
            .appendImpl("begin")
            .appendImpl("  Foo(@(FormatSettings));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testForLoopVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Foo(Int: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  I: Integer;")
            .appendImpl("begin")
            .appendImpl("  for I := 0 to 100 do begin")
            .appendImpl("    Foo(I);")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testInitializedInRepeatLoopShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  I: Integer;")
            .appendImpl("begin")
            .appendImpl("  repeat")
            .appendImpl("    I := 0;")
            .appendImpl("  until I = 0;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testSelfReferencingAssignmentShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  I: Integer;")
            .appendImpl("begin")
            .appendImpl("  I := I + 1;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 5));
  }

  @Test
  void testAssigneeNestedReferenceShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test(Arr: array of Integer);")
            .appendImpl("var")
            .appendImpl("  I: Integer;")
            .appendImpl("begin")
            .appendImpl("  Arr[I] := 1;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 5));
  }

  @Test
  void testPassedAsVarParameterShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure GetInt(var Int: Integer);")
            .appendDecl("procedure Foo(Int: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  I: Integer;")
            .appendImpl("begin")
            .appendImpl("  GetInt(I);")
            .appendImpl("  Foo(I);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testTextFilePassedAsVarParameterShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  T: TextFile;")
            .appendImpl("begin")
            .appendImpl("  AssignFile(T, 'Foo');")
            .appendImpl("  WriteLn(T, 'Bar');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testPassedAsOutParameterShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure GetInt(out Int: Integer);")
            .appendDecl("procedure Foo(Int: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  I: Integer;")
            .appendImpl("begin")
            .appendImpl("  GetInt(I);")
            .appendImpl("  Foo(I);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testRecordFieldPassedAsVarParameterShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBar = record")
            .appendDecl("    Baz: Integer;")
            .appendDecl("  end;")
            .appendDecl("procedure GetInt(var Int: Integer);")
            .appendDecl("procedure Foo(Int: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  GetInt(Bar.Baz);")
            .appendImpl("  Foo(Bar.Baz);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testPassedToProcVarShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TTestProc = reference to procedure(Int: Integer);")
            .appendImpl("procedure Test(Proc: TTestProc);")
            .appendImpl("var")
            .appendImpl("  I: Integer;")
            .appendImpl("begin")
            .appendImpl("  Proc(I);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 5));
  }

  @Test
  void testPassedToProcVarOutParameterShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TTestProc = reference to procedure(out Int: Integer);")
            .appendDecl("procedure Foo(Int: Integer);")
            .appendImpl("procedure Test(Proc: TTestProc);")
            .appendImpl("var")
            .appendImpl("  I: Integer;")
            .appendImpl("begin")
            .appendImpl("  Proc(I);")
            .appendImpl("  Foo(I);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testPassedToArrayProcVarOutParameterFalsePositiveShouldFailOnUpgrade() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TTestProc = reference to procedure(out Int: Integer);")
            .appendDecl("procedure Foo(Int: Integer);")
            .appendImpl("procedure Test(ProcArray: array of TTestProc);")
            .appendImpl("var")
            .appendImpl("  I: Integer;")
            .appendImpl("begin")
            .appendImpl("  ProcArray[0](I);")
            .appendImpl("  Foo(I);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 5));
  }

  @Test
  void testPassedToProcVarVarParameterShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TTestProc = reference to procedure(var Int: Integer);")
            .appendDecl("procedure Foo(Int: Integer);")
            .appendImpl("procedure Test(Proc: TTestProc);")
            .appendImpl("var")
            .appendImpl("  I: Integer;")
            .appendImpl("begin")
            .appendImpl("  Proc(I);")
            .appendImpl("  Foo(I);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testPartiallyInitializedRecordInQualifiedReferenceShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBar = record")
            .appendDecl("    Baz: Integer;")
            .appendDecl("    Flarp: Integer;")
            .appendDecl("  end;")
            .appendDecl("procedure Foo(Int: Integer);")
            .appendDecl("function Oof(Int: Integer): Integer;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("  Blam: Integer;")
            .appendImpl("begin")
            .appendImpl("  Bar.Baz := 123;")
            .appendImpl("  Foo(Bar.Baz);")
            .appendImpl("  Blam := Oof(Bar.Baz);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testPassedAsPointerShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  PInteger = ^Integer;")
            .appendDecl("procedure GetInt(I: PInteger);")
            .appendDecl("procedure Foo(Int: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  I: Integer;")
            .appendImpl("begin")
            .appendImpl("  GetInt(@I);")
            .appendImpl("  Foo(I);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testUninitializedRecordFieldPassedAsUntypedVarParameterShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBar = record")
            .appendDecl("    Int: Integer;")
            .appendDecl("  end;")
            .appendDecl("procedure GetInt(var Int);")
            .appendDecl("procedure Foo(Int: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  GetInt(Bar.Int);")
            .appendImpl("  Foo(Bar.Int);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testHardCastedPointerPassedAsVarParameterShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  PInteger = ^Integer;")
            .appendDecl("procedure GetInt(var P: PInteger);")
            .appendDecl("procedure Foo(P: Pointer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  P: Pointer;")
            .appendImpl("begin")
            .appendImpl("  GetInt(PInteger(P));")
            .appendImpl("  Foo(P);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testHardCastedAssignmentShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Foo(P: Pointer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  P: Pointer;")
            .appendImpl("begin")
            .appendImpl("  Integer(P) := 123;")
            .appendImpl("  Foo(P);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testSelfReferencingAssignmentPassedAsVarParameterShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure GetInt(var I: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  I: Integer;")
            .appendImpl("begin")
            .appendImpl("  I := GetInt(I);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testSelfReferencingAssignmentPassedAsOutParameterShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure GetInt(out I: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  I: Integer;")
            .appendImpl("begin")
            .appendImpl("  I := GetInt(I);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testUninitializedObjectWithAssignedCheckShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    procedure Bar;")
            .appendDecl("  end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  if Assigned(Foo) then begin")
            .appendImpl("    Foo.Bar;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 5))
        .areNot(ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 6));
  }

  @Test
  void testSizeOfShouldNotAddIssueOrAffectInitializationState() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Foo(I: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  I: Integer;")
            .appendImpl("  Size: Integer;")
            .appendImpl("begin")
            .appendImpl("  Size := SizeOf(I);")
            .appendImpl("  Foo(I);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areNot(ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 7));
  }

  @Test
  void testUnknownMethodArgumentShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  I: Integer;")
            .appendImpl("begin")
            .appendImpl("  UnknownMethod(Foo(I));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testUninitializedObjectWithFreeAndNilShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("  System.SysUtils;")
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    procedure Bar;")
            .appendDecl("  end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  FreeAndNil(Foo);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 5));
  }

  @Test
  void testUnionVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Foo(I: Integer);")
            .appendImpl("procedure Test(P: Pointer);")
            .appendImpl("var")
            .appendImpl("  I: Integer absolute P;")
            .appendImpl("begin")
            .appendImpl("  Foo(I);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testInitializedInSubprocedureShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Foo(I: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("  var")
            .appendImpl("    I: Integer;")
            .appendImpl("  procedure Sub;")
            .appendImpl("  begin")
            .appendImpl("    I := 123;")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  Sub;")
            .appendImpl("  Foo(I);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testUsedBeforeInitializedInSubprocedureShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Foo(I: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("  var")
            .appendImpl("    I: Integer;")
            .appendImpl("  procedure Sub;")
            .appendImpl("  begin")
            .appendImpl("    I := 123;")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  Foo(I);")
            .appendImpl("  Sub;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VariableInitializationRule", builder.getOffset() + 9));
  }

  @Test
  void testRecordAfterAnyMethodCalledShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBar = record")
            .appendDecl("    Baz: Integer;")
            .appendDecl("    procedure Reset;")
            .appendDecl("  end;")
            .appendDecl("procedure Foo(Int: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Bar.Reset;")
            .appendImpl("  Foo(Bar.Baz);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }

  @Test
  void testRecordAfterAnyPropertyReferenceShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBar = record")
            .appendDecl("    Baz: Integer;")
            .appendDecl("    FBlam: Integer;")
            .appendDecl("    property Blam: Integer write FBlam;")
            .appendDecl("  end;")
            .appendDecl("procedure Foo(Int: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Bar.Blam := 123;")
            .appendImpl("  Foo(Bar.Baz);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableInitializationRule"));
  }
}
