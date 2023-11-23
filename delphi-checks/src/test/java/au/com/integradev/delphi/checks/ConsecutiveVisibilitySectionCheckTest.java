/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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

class ConsecutiveVisibilitySectionCheckTest {
  @Test
  void testConsecutiveVisibilitySectionsOfSameTypeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveVisibilitySectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private")
                .appendDecl("    FBar: Integer;")
                .appendDecl("  private // Noncompliant")
                .appendDecl("    FBaz: Integer;")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testMultipleConsecutiveVisibilitySectionsOfSameTypeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveVisibilitySectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private")
                .appendDecl("    FBar: Integer;")
                .appendDecl("  private // Noncompliant")
                .appendDecl("    FBaz: Integer;")
                .appendDecl("  private // Noncompliant")
                .appendDecl("    Flarp: Integer;")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testClassVarVisibilitySectionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveVisibilitySectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private")
                .appendDecl("    FBar: Integer;")
                .appendDecl("  private class var")
                .appendDecl("    FBaz: Integer;")
                .appendDecl("  private")
                .appendDecl("    Flarp: Integer;")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testTypeVisibilitySectionsOfSameTypeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveVisibilitySectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private type")
                .appendDecl("    TBar = Integer;")
                .appendDecl("  private type // Noncompliant")
                .appendDecl("    TBaz = Integer;")
                .appendDecl("  private")
                .appendDecl("    Flarp: Integer;")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testSingleItemTypeSectionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveVisibilitySectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private type")
                .appendDecl("    TBaz = record")
                .appendDecl("      FBar: string;")
                .appendDecl("      procedure Bar;")
                .appendDecl("    end;")
                .appendDecl("  private")
                .appendDecl("    Flarp: Integer;")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testStartMultiItemVisibilitySectionsOfSameTypeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveVisibilitySectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private")
                .appendDecl("    type")
                .appendDecl("      TBar = Integer;")
                .appendDecl("    FBaz: TBar;")
                .appendDecl("  private // Noncompliant")
                .appendDecl("    Flarp: Integer;")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testEndMultiItemVisibilitySectionsOfSameTypeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveVisibilitySectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private")
                .appendDecl("    Flarp: Integer;")
                .appendDecl("  private // Noncompliant")
                .appendDecl("    type")
                .appendDecl("      TBar = Integer;")
                .appendDecl("    FBaz: TBar;")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testConstVisibilitySectionsOfSameTypeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveVisibilitySectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private const")
                .appendDecl("    CBar = 4;")
                .appendDecl("  private const // Noncompliant")
                .appendDecl("    CBaz = 5;")
                .appendDecl("  private")
                .appendDecl("    Flarp: Integer;")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testConsecutiveTypeAndConstVisibilitySectionsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveVisibilitySectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private type")
                .appendDecl("    TBar = Integer;")
                .appendDecl("  private const")
                .appendDecl("    CBaz = 5;")
                .appendDecl("")
                .appendDecl("  private")
                .appendDecl("    Flarp: Integer;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testConsecutiveFieldVisibilitySectionsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveVisibilitySectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private var")
                .appendDecl("    FBar: Integer;")
                .appendDecl("  private // Noncompliant")
                .appendDecl("    Flarp: Integer;")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testConsecutiveFieldVisibilitySectionsFollowedByConsecutiveTypeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveVisibilitySectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private var")
                .appendDecl("    FBar: Integer;")
                .appendDecl("  private // Noncompliant")
                .appendDecl("    Flarp: Integer;")
                .appendDecl("  private type")
                .appendDecl("    TBaz = Integer;")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testConsecutiveVisibilitySectionsOfDifferentTypeShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveVisibilitySectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private")
                .appendDecl("    FBar: Integer;")
                .appendDecl("  public")
                .appendDecl("    FFlorp: Integer;")
                .appendDecl("  private")
                .appendDecl("    FBaz: Integer;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }
}
