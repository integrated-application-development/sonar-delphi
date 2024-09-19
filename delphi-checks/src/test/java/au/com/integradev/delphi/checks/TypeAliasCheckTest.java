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

class TypeAliasCheckTest {

  @Test
  void testTypeAliasShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TypeAliasCheck())
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("type")
                .appendDecl("  TMyChar = Char; // Noncompliant"))
        .verifyIssues();
  }

  @Test
  void testTypeAliasNewTypeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TypeAliasCheck())
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("type")
                .appendDecl("  TMyChar = type Char; // Noncompliant"))
        .verifyIssues();
  }

  @Test
  void testClassReferenceShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TypeAliasCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyClass = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  TMetaClass = class of TMyClass;"))
        .verifyNoIssues();
  }

  @Test
  void testEmptyRecordShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TypeAliasCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyRecord = record")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testEmptyClassShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TypeAliasCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyClass = class(TObject)")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testSetsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TypeAliasCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TSetOfChar = set of Char;"))
        .verifyNoIssues();
  }

  @Test
  void testArraysShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TypeAliasCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TArrayOfChar = array of Char;"))
        .verifyNoIssues();
  }

  @Test
  void testSubRangesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TypeAliasCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TSubRange = Lower..Upper;"))
        .verifyNoIssues();
  }

  @Test
  void testPointerTypesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TypeAliasCheck())
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("type")
                .appendDecl("  PClass = ^TClass;"))
        .verifyNoIssues();
  }
}
