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

class ImportSpecificityCheckTest {
  @Test
  void testImportUsedInImplementationShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ImportSpecificityCheck())
        .withSearchPathUnit(createSystemUITypes())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  System.UITypes;")
                .appendImpl("type")
                .appendImpl("  Alias = System.UITypes.TMsgDlgType;"))
        .verifyIssueOnLine(6);
  }

  @Test
  void testImportUsedInInterfaceShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ImportSpecificityCheck())
        .withSearchPathUnit(createSystemUITypes())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  System.UITypes;")
                .appendDecl("type")
                .appendDecl("  Alias = System.UITypes.TMsgDlgType;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedImportShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ImportSpecificityCheck())
        .withSearchPathUnit(createSystemUITypes())
        .onFile(new DelphiTestUnitBuilder().appendImpl("uses System.UITypes;"))
        .verifyNoIssues();
  }

  private static DelphiTestUnitBuilder createSystemUITypes() {
    return new DelphiTestUnitBuilder()
        .unitName("System.UITypes")
        .appendDecl("{$SCOPEDENUMS ON}")
        .appendDecl("type")
        .appendDecl(
            "  TMsgDlgType = (mtWarning, mtError, mtInformation, mtConfirmation, mtCustom);")
        .appendDecl("  TMsgDlgBtn = (mbYes, mbNo, mbOK, mbCancel, mbAbort, mbRetry, mbIgnore,")
        .appendDecl("    mbAll, mbNoToAll, mbYesToAll, mbHelp, mbClose);")
        .appendDecl("  TMsgDlgButtons = set of TMsgDlgBtn;");
  }
}
