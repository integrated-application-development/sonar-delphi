package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class DateFormatSettingsRuleTest extends BasePmdRuleTest {
  @Test
  void testDefaultTFormatSettingsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("uses")
            .appendImpl("    System.SysUtils")
            .appendImpl("  ;")
            .appendImpl("procedure Test;")
            .appendImpl("const")
            .appendImpl("  C_Epoch = '1/1/1970';")
            .appendImpl("var")
            .appendImpl("  DateTime: TDateTime;")
            .appendImpl("begin")
            .appendImpl("  DateTime := StrToDateTime(C_Epoch);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("DateFormatSettingsRule", builder.getOffset() + 10));
  }

  @Test
  void testProvidedTFormatSettingsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("uses")
            .appendImpl("    System.SysUtils")
            .appendImpl("  ;")
            .appendImpl("procedure Test;")
            .appendImpl("const")
            .appendImpl("  C_Epoch = '1/1/1970';")
            .appendImpl("var")
            .appendImpl("  FormatSettings: TFormatSettings;")
            .appendImpl("  DateTime: TDateTime;")
            .appendImpl("begin")
            .appendImpl("  FormatSettings := TFormatSettings.Create;")
            .appendImpl("  DateTime := StrToDateTime(C_Epoch, FormatSettings);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
