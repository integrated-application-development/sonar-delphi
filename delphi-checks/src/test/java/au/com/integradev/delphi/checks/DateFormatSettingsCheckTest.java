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

class DateFormatSettingsCheckTest {
  @Test
  void testDefaultTFormatSettingsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new DateFormatSettingsCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
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
                .appendImpl("  DateTime := StrToDateTime(C_Epoch); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testProvidedTFormatSettingsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new DateFormatSettingsCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
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
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  private static DelphiTestUnitBuilder createSysUtils() {
    return new DelphiTestUnitBuilder()
        .unitName("System.SysUtils")
        .appendDecl("type")
        .appendDecl("  TFormatSettings = record")
        .appendDecl("    type")
        .appendDecl("      TEraInfo = record")
        .appendDecl("        EraName: string;")
        .appendDecl("        EraOffset: Integer;")
        .appendDecl("        EraStart: TDate;")
        .appendDecl("        EraEnd: TDate;")
        .appendDecl("      end;")
        .appendDecl("    public")
        .appendDecl("      CurrencyString: string;")
        .appendDecl("      CurrencyFormat: Byte;")
        .appendDecl("      CurrencyDecimals: Byte;")
        .appendDecl("      DateSeparator: Char;")
        .appendDecl("      TimeSeparator: Char;")
        .appendDecl("      ListSeparator: Char;")
        .appendDecl("      ShortDateFormat: string;")
        .appendDecl("      LongDateFormat: string;")
        .appendDecl("      TimeAMString: string;")
        .appendDecl("      TimePMString: string;")
        .appendDecl("      ShortTimeFormat: string;")
        .appendDecl("      LongTimeFormat: string;")
        .appendDecl("      ShortMonthNames: array[1..12] of string;")
        .appendDecl("      LongMonthNames: array[1..12] of string;")
        .appendDecl("      ShortDayNames: array[1..7] of string;")
        .appendDecl("      LongDayNames: array[1..7] of string;")
        .appendDecl("      EraInfo: array of TEraInfo;")
        .appendDecl("      ThousandSeparator: Char;")
        .appendDecl("      DecimalSeparator: Char;")
        .appendDecl("      TwoDigitYearCenturyWindow: Word;")
        .appendDecl("      NegCurrFormat: Byte;")
        .appendDecl("      NormalizedLocaleName: string;")
        .appendDecl("")
        .appendDecl("      class function Create: TFormatSettings; overload; static; inline;")
        .appendDecl(
            "      class function Create(Locale: TLocaleID): TFormatSettings; overload; platform;"
                + " static;")
        .appendDecl(
            "      class function Create(const LocaleName: string): TFormatSettings; overload;"
                + " static;")
        .appendDecl("      class function Invariant: TFormatSettings; static;")
        .appendDecl("  end;")
        .appendDecl("")
        .appendDecl("  function StrToDateTime(const S: string): TDateTime; overload; inline;")
        .appendDecl("  function StrToDateTime(const S: string;")
        .appendDecl("    const AFormatSettings: TFormatSettings): TDateTime; overload;");
  }
}
