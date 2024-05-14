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

import au.com.integradev.delphi.builders.DelphiTestProgramBuilder;
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
                .appendDecl("  System.UITypes; // Noncompliant")
                .appendImpl("type")
                .appendImpl("  Alias = System.UITypes.TMsgDlgType;"))
        .verifyIssues();
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

  @Test
  void testUnusedDprImportShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ImportSpecificityCheck())
        .withSearchPathUnit(createSystemUITypes())
        .onFile(
            new DelphiTestProgramBuilder()
                .appendDecl("uses")
                .appendDecl("  System.UITypes;")
                .appendImpl("var DlgType := mtWarning;"))
        .verifyNoIssues();
  }

  @Test
  void testImportUsedInImplementationShouldAddQuickFix() {
    CheckVerifier.newVerifier()
        .withCheck(new ImportSpecificityCheck())
        .withSearchPathUnit(createSystemUITypes())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("// Fix@[+1:0 to +2:17] <<>>")
                .appendDecl("uses")
                .appendDecl("  System.UITypes; // Noncompliant")
                .appendImpl("// Fix@[+2:20 to +2:20] <<, >>")
                .appendImpl("// Fix@[+1:20 to +1:20] <<System.UITypes>>")
                .appendImpl("uses System.SysUtils;")
                .appendImpl("type")
                .appendImpl("  Alias = System.UITypes.TMsgDlgType;"))
        .verifyIssues();
  }

  @Test
  void testSingleImportUsedInImplementationWithNoUsesClauseShouldAddQuickFix() {
    CheckVerifier.newVerifier()
        .withCheck(new ImportSpecificityCheck())
        .withSearchPathUnit(createSystemUITypes())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("// Fix@[+1:0 to +2:17] <<>>")
                .appendDecl("uses")
                .appendDecl("  System.UITypes; // Noncompliant")
                .appendImpl("// Fix@[-2:14 to -2:14] <<\\r\\n\\r\\nuses >>")
                .appendImpl("// Fix@[-3:14 to -3:14] <<System.UITypes>>")
                .appendImpl("// Fix@[-4:14 to -4:14] <<;>>")
                .appendImpl("type")
                .appendImpl("  Alias = System.UITypes.TMsgDlgType;"))
        .verifyIssues();
  }

  @Test
  void testFirstImportUsedInImplementationWithNoUsesClauseShouldAddQuickFix() {
    CheckVerifier.newVerifier()
        .withCheck(new ImportSpecificityCheck())
        .withSearchPathUnit(createSystemUITypes())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("// Fix@[+2:2 to +3:2] <<>>")
                .appendDecl("uses")
                .appendDecl("  System.UITypes, // Noncompliant")
                .appendDecl("  System.SysUtils;")
                .appendImpl("// Fix@[-2:14 to -2:14] <<\\r\\n\\r\\nuses >>")
                .appendImpl("// Fix@[-3:14 to -3:14] <<System.UITypes>>")
                .appendImpl("// Fix@[-4:14 to -4:14] <<;>>")
                .appendImpl("type")
                .appendImpl("  Alias = System.UITypes.TMsgDlgType;"))
        .verifyIssues();
  }

  @Test
  void testNthImportUsedInImplementationWithNoUsesClauseShouldAddQuickFix() {
    CheckVerifier.newVerifier()
        .withCheck(new ImportSpecificityCheck())
        .withSearchPathUnit(createSystemUITypes())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("// Fix@[+1:16 to +2:16] <<>>")
                .appendDecl("  System.Contnrs,")
                .appendDecl("  System.UITypes, // Noncompliant")
                .appendDecl("  System.SysUtils;")
                .appendImpl("// Fix@[-2:14 to -2:14] <<\\r\\n\\r\\nuses >>")
                .appendImpl("// Fix@[-3:14 to -3:14] <<System.UITypes>>")
                .appendImpl("// Fix@[-4:14 to -4:14] <<;>>")
                .appendImpl("type")
                .appendImpl("  Alias = System.UITypes.TMsgDlgType;"))
        .verifyIssues();
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
