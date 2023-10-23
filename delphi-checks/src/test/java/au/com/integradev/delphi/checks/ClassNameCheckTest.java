/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colombo
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

class ClassNameCheckTest {

  @Test
  void testClassNameWithPrefixShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ClassNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TType = class(TObject)")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testClassNameWithWrongCasePrefixShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ClassNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  tType = class(TObject) // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testNestedClassesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ClassNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TOuterClass = class(TObject)")
                .appendDecl("  strict private")
                .appendDecl("    type")
                .appendDecl("      TInnerClass1 = class(TObject)")
                .appendDecl("      end;")
                .appendDecl("      TInnerClass2 = class(TObject)")
                .appendDecl("      end;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testAttributeClassNameShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ClassNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses System;")
                .appendDecl("type")
                .appendDecl("  my_attribute = class(TCustomAttribute)")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testClassHelperNameShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ClassNameCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  my_helper = class helper for TObject")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }
}
