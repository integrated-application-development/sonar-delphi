package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class FormatSettingsInitializationRuleTest extends BasePmdRuleTest {
  @Test
  void testFormatSettingsCreateShouldNotAddIssue() {
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

    assertIssues().isEmpty();
  }

  @Test
  void testFormatSettingsInvariantShouldNotAddIssue() {
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
            .appendImpl("  FormatSettings := TFormatSettings.Invariant;")
            .appendImpl("  Foo(FormatSettings);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testFormatSettingsAssignedFromExternalVariableShouldNotAddIssue() {
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

    assertIssues().areNot(ruleKey("FormatSettingsInitializationRule"));
  }

  @Test
  void testFormatSettingsAssignedFromLocalInitializedVariableShouldNotAddIssue() {
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

    assertIssues().isEmpty();
  }

  @Test
  void testFormatSettingsAssignedFromLocalUninitializedVariableShouldAddIssue() {
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
            .appendImpl("  FormatSettings2 := FormatSettings1;")
            .appendImpl("  Foo(FormatSettings2);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("FormatSettingsInitializationRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("FormatSettingsInitializationRule", builder.getOffset() + 7));
  }

  @Test
  void testInlineFormatSettingsAssignedFromLocalInitializedVariableShouldNotAddIssue() {
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

    assertIssues().areNot(ruleKey("FormatSettingsInitializationRule"));
  }

  @Test
  void testInlineFormatSettingsAssignedFromLocalUninitializedVariableShouldAddIssue() {
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
        .areExactly(1, ruleKeyAtLine("FormatSettingsInitializationRule", builder.getOffset() + 4))
        .areExactly(1, ruleKeyAtLine("FormatSettingsInitializationRule", builder.getOffset() + 5));
  }

  @Test
  void testInlineFormatSettingsCreateShouldNotAddIssue() {
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

    assertIssues().isEmpty();
  }

  @Test
  void testInlineFormatSettingsCreateWithImmediateInitializationShouldNotAddIssue() {
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

    assertIssues().areNot(ruleKey("FormatSettingsInitializationRule"));
  }

  @Test
  void testNestedFormatSettingsCreateShouldNotAddIssue() {
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
            .appendImpl("    FormatSettings := TFormatSettings.Create;")
            .appendImpl("    Foo(FormatSettings);")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("FormatSettingsInitializationRule"));
  }

  @Test
  void testUnusedFormatSettingsShouldNotAddIssue() {
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

    assertIssues().isEmpty();
  }

  @Test
  void testFormatSettingsWithoutInitializationShouldAddIssue() {
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
        .areExactly(1, ruleKeyAtLine("FormatSettingsInitializationRule", builder.getOffset() + 5));
  }

  @Test
  void testFormatSettingsWithoutInitializationShouldOnlyAddOneIssue() {
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

    assertIssues()
        .areNot(ruleKeyAtLine("FormatSettingsInitializationRule", builder.getOffset() + 6));
  }

  @Test
  void testInlineFormatSettingsWithoutInitializationShouldAddIssue() {
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
        .areExactly(1, ruleKeyAtLine("FormatSettingsInitializationRule", builder.getOffset() + 4));
  }

  @Test
  void testMultipleInlineFormatSettingsWithoutInitializationShouldAddIssue() {
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
        .areExactly(1, ruleKeyAtLine("FormatSettingsInitializationRule", builder.getOffset() + 4))
        .areExactly(1, ruleKeyAtLine("FormatSettingsInitializationRule", builder.getOffset() + 5))
        .areExactly(1, ruleKeyAtLine("FormatSettingsInitializationRule", builder.getOffset() + 6));
  }
}
