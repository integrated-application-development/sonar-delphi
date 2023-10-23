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

import au.com.integradev.delphi.builders.DelphiTestFile;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UnitLevelKeywordIndentationCheckTest {
  private static final String RESOURCE_PATH =
      "/au/com/integradev/delphi/checks/UnitLevelKeywordIndentationCheck";
  private static final String CORRECT_PAS = RESOURCE_PATH + "/Correct.pas";
  private static final String INDENTED_PAS = RESOURCE_PATH + "/Indented.pas";
  private static final String CORRECT_DPR = RESOURCE_PATH + "/Correct.dpr";
  private static final String INDENTED_DPR = RESOURCE_PATH + "/Indented.dpr";

  @Test
  void testIndentedProgramShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnitLevelKeywordIndentationCheck())
        .onFile(DelphiTestFile.fromResource(INDENTED_DPR))
        .verifyIssues();
  }

  @Test
  void testIndentedUnitShouldAddIssues() {
    CheckVerifier.newVerifier()
        .withCheck(new UnitLevelKeywordIndentationCheck())
        .onFile(DelphiTestFile.fromResource(INDENTED_PAS))
        .verifyIssues();
  }

  @Test
  void testIndentedInnerTypeShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnitLevelKeywordIndentationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TObject = class(TObject)")
                .appendDecl("    type TInnerObject = class(TObject)")
                .appendDecl("    end;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {CORRECT_PAS, CORRECT_DPR})
  void testUnindentedKeywordsShouldNotAddIssue(String correctFile) {
    CheckVerifier.newVerifier()
        .withCheck(new UnitLevelKeywordIndentationCheck())
        .onFile(DelphiTestFile.fromResource(correctFile))
        .verifyNoIssues();
  }
}
