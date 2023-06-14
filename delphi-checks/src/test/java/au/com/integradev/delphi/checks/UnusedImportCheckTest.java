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

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

class UnusedImportCheckTest {
  private static DelphiCheck createCheck() {
    return createCheck("");
  }

  private static DelphiCheck createCheck(String exclusions) {
    UnusedImportCheck check = new UnusedImportCheck();
    check.exclusions = exclusions;
    return check;
  }

  @Test
  void testUnusedImportShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Obj: TObject;")
                .appendImpl("begin")
                .appendImpl("  Obj := TObject.Create;")
                .appendImpl("end;"))
        .verifyIssueOnLine(8);
  }

  @Test
  void testUnresolvedImportShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  NONEXISTENT_UNIT;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Obj: TObject;")
                .appendImpl("begin")
                .appendImpl("  Obj := TObject.Create;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testImplicitlyUsedImportShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Obj: TObject;")
                .appendImpl("begin")
                .appendImpl("  Obj := TObject.Create;")
                .appendImpl("  FreeAndNil(Obj);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testExcludedUnusedImportInInterfaceSectionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck("System.SysUtils"))
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  System.SysUtils;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Obj: TObject;")
                .appendImpl("begin")
                .appendImpl("  Obj := TObject.Create;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testExcludedUnusedImportInImplementationSectionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck("System.SysUtils"))
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Obj: TObject;")
                .appendImpl("begin")
                .appendImpl("  Obj := TObject.Create;")
                .appendImpl("end;"))
        .verifyIssueOnLine(8);
  }

  private static DelphiTestUnitBuilder createSysUtils() {
    return new DelphiTestUnitBuilder()
        .unitName("System.SysUtils")
        .appendDecl("procedure FreeAndNil(var Obj); inline;");
  }
}
