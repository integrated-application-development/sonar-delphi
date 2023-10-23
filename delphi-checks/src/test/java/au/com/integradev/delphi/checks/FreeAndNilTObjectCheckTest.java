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

class FreeAndNilTObjectCheckTest {
  @Test
  void testTObjectShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FreeAndNilTObjectCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  System.SysUtils;")
                .appendImpl("procedure Foo(Bar: TObject);")
                .appendImpl("begin")
                .appendImpl("  FreeAndNil(Bar);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testDoubleShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FreeAndNilTObjectCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  System.SysUtils;")
                .appendImpl("procedure Foo(Bar: Double);")
                .appendImpl("begin")
                .appendImpl("  FreeAndNil(Bar); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  private static DelphiTestUnitBuilder createSysUtils() {
    return new DelphiTestUnitBuilder()
        .unitName("System.SysUtils")
        .appendDecl("procedure FreeAndNil(var Obj); inline;");
  }
}
