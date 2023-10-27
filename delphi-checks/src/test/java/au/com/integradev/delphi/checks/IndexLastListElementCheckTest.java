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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IndexLastListElementCheckTest {
  private static DelphiTestUnitBuilder systemClassesWithTList() {
    return new DelphiTestUnitBuilder()
        .unitName("System.Classes")
        .appendDecl("type")
        .appendDecl("  TList = class")
        .appendDecl("    FCount: Integer;")
        .appendDecl("    function GetItem(Index: Integer): TObject;")
        .appendDecl("    property Items[Index: Integer]: TObject read GetItem; default;")
        .appendDecl("    property Count: Integer read FCount;")
        .appendDecl("  end;");
  }

  private static DelphiTestUnitBuilder systemContnrsWithTObjectList() {
    return new DelphiTestUnitBuilder()
        .unitName("System.Contnrs")
        .appendDecl("uses")
        .appendDecl("  System.Classes;")
        .appendDecl("type")
        .appendDecl("  TObjectList = class(TList)")
        .appendDecl("  end;");
  }

  private static DelphiTestUnitBuilder systemGenericsCollections() {
    return new DelphiTestUnitBuilder()
        .unitName("System.Generics.Collections")
        .appendDecl("type")
        .appendDecl("  TList<T> = class")
        .appendDecl("    FCount: Integer;")
        .appendDecl("    function GetItem(Index: Integer): T;")
        .appendDecl("    property Items[Index: Integer]: T read GetItem; default;")
        .appendDecl("    property Count: Integer read FCount;")
        .appendDecl("  end;")
        .appendDecl("")
        .appendDecl("  TObjectList<T> = class(TList<T>)")
        .appendDecl("  end;");
  }

  @Test
  void testTListDescendantShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IndexLastListElementCheck())
        .withStandardLibraryUnit(systemClassesWithTList())
        .withStandardLibraryUnit(systemContnrsWithTObjectList())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.Contnrs;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  List: TObjectList;")
                .appendImpl("begin")
                .appendImpl("  // Fix@[+1:6 to +1:22] <<.Last>>")
                .appendImpl("  List[List.Count - 1]; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNonTListDescendantShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IndexLastListElementCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("type")
                .appendImpl("  TList = class")
                .appendImpl("    FCount: Integer;")
                .appendImpl("    function GetItem(Index: Integer): TObject;")
                .appendImpl("    property Items[Index: Integer]: TObject read GetItem; default;")
                .appendImpl("    property Count: Integer read FCount;")
                .appendImpl("  end;")
                .appendImpl("")
                .appendImpl("procedure TList.GetItem(Index: Integer): TObject; begin end;")
                .appendImpl("")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  List: TList;")
                .appendImpl("begin")
                .appendImpl("  List[List.Count - 1];")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testGenericTListShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IndexLastListElementCheck())
        .withStandardLibraryUnit(systemGenericsCollections())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.Generics.Collections;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  List: TList<Integer>;")
                .appendImpl("begin")
                .appendImpl("  List[List.Count - 1]; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testGenericTObjectListShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IndexLastListElementCheck())
        .withStandardLibraryUnit(systemGenericsCollections())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.Generics.Collections;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  List: TObjectList<Integer>;")
                .appendImpl("begin")
                .appendImpl("  List[List.Count - 1]; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "List[List2.Count - 1]",
        "List2[List.Count - 1]",
        "List[List.Count]",
        "List[List.Count + 1]",
        "List[List.Count - 1 - 1]",
        "List[+List.Count - 1]",
        "List[List.Count - 0]",
      })
  void testNotMatchingExpressionShouldNotAddIssue(String use) {
    CheckVerifier.newVerifier()
        .withCheck(new IndexLastListElementCheck())
        .withStandardLibraryUnit(systemClassesWithTList())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.Classes;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  List: TList;")
                .appendImpl("  List2: TList;")
                .appendImpl("begin")
                .appendImpl("  " + use)
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "List[List.Count - 1]",
        "List[(List.Count - 1)]",
        "List[(List.Count) - (1)]",
        "List[List.Count - (1)]",
        "List[(((List.Count)) - ((1)))]",
        "List[Integer(List[List.Count - 1])]",
      })
  void testMatchingExpressionShouldAddIssue(String use) {
    CheckVerifier.newVerifier()
        .withCheck(new IndexLastListElementCheck())
        .withStandardLibraryUnit(systemClassesWithTList())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.Classes;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  List: TList;")
                .appendImpl("begin")
                .appendImpl("  " + use + " // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
