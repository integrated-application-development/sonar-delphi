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

class GroupedParameterDeclarationCheckTest {

  @Test
  void testSingleParameterDeclarationsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new GroupedParameterDeclarationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test(Foo: Integer; Bar: Integer);")
                .appendImpl("begin")
                .appendImpl("  // Do nothing")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testMultipleParameterDeclarationShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new GroupedParameterDeclarationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("// Fix@[+3:18 to +3:19] <<;>>")
                .appendImpl("// Fix@[+2:18 to +2:18] <<Integer>>")
                .appendImpl("// Fix@[+1:18 to +1:18] <<: >>")
                .appendImpl("procedure Test(Foo, Bar: Integer); // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  // Do nothing")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testConstParameterKeywordShouldAddQuickFix() {
    CheckVerifier.newVerifier()
        .withCheck(new GroupedParameterDeclarationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("// Fix@[+4:24 to +4:25] <<;>>")
                .appendImpl("// Fix@[+3:24 to +3:24] <<Integer>>")
                .appendImpl("// Fix@[+2:24 to +2:24] <<: >>")
                .appendImpl("// Fix@[+1:26 to +1:26] <<const >>")
                .appendImpl("procedure Test(const Foo, Bar: Integer); // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  // Do nothing")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testThreeParametersShouldAddQuickFix() {
    CheckVerifier.newVerifier()
        .withCheck(new GroupedParameterDeclarationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("// Fix@[+8:24 to +8:25] <<;>>")
                .appendImpl("// Fix@[+7:24 to +7:24] <<Integer>>")
                .appendImpl("// Fix@[+6:24 to +6:24] <<: >>")
                .appendImpl("// Fix@[+5:26 to +5:26] <<const >>")
                .appendImpl("// Fix@[+4:29 to +4:30] <<;>>")
                .appendImpl("// Fix@[+3:29 to +3:29] <<Integer>>")
                .appendImpl("// Fix@[+2:29 to +2:29] <<: >>")
                .appendImpl("// Fix@[+1:31 to +1:31] <<const >>")
                .appendImpl("procedure Test(const Foo, Bar, Baz: Integer); // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  // Do nothing")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
