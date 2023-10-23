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

class NonLinearCastCheckTest {
  @Test
  void testCastToGrandparentClassShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NonLinearCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TGrandparent = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  TParent = class(TGrandparent)")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TParent)")
                .appendDecl("  end;")
                .appendImpl("function AsGrandparent(Obj: TChild): TGrandparent;")
                .appendImpl("begin")
                .appendImpl("  Result := TGrandparent(Obj);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testCastToParentClassShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NonLinearCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TParent = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TParent)")
                .appendDecl("  end;")
                .appendImpl("function AsParent(Obj: TChild): TParent;")
                .appendImpl("begin")
                .appendImpl("  Result := TParent(Obj);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testCastToChildClassShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NonLinearCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TParent = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TParent)")
                .appendDecl("  end;")
                .appendImpl("function AsChild(Obj: TParent): TChild;")
                .appendImpl("begin")
                .appendImpl("  Result := TChild(Obj);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testCastToGrandchildClassShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NonLinearCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TParent = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TParent)")
                .appendDecl("  end;")
                .appendDecl("  TGrandchild = class(TChild)")
                .appendDecl("  end;")
                .appendImpl("function AsGrandchild(Obj: TParent): TGrandchild;")
                .appendImpl("begin")
                .appendImpl("  Result := TGrandchild(Obj);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testCastToTObjectShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NonLinearCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyType = class(TObject)")
                .appendDecl("  end;")
                .appendImpl("function AsObject(Obj: TMyType): TObject;")
                .appendImpl("begin")
                .appendImpl("  Result := TObject(Obj);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testCastToSelfShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NonLinearCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyType = class(TObject)")
                .appendDecl("  end;")
                .appendImpl("function AsSame(Obj: TMyType): TMyType;")
                .appendImpl("begin")
                .appendImpl("  Result := TMyType(Obj);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testCastToSiblingClassShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NonLinearCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TParent = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TParent)")
                .appendDecl("  end;")
                .appendDecl("  TOtherChild = class(TParent)")
                .appendDecl("  end;")
                .appendImpl("function AsOtherChild(Obj: TChild): TOtherChild;")
                .appendImpl("begin")
                .appendImpl("  Result := TOtherChild(Obj); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testCastToUnrelatedObjectShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NonLinearCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TDog = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  TCat = class(TObject)")
                .appendDecl("  end;")
                .appendImpl("function AsCat(Obj: TDog): TCat;")
                .appendImpl("begin")
                .appendImpl("  Result := TCat(Obj); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testCastToUntypedPointerShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NonLinearCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyType = class(TObject)")
                .appendDecl("  end;")
                .appendImpl("function AsPointer(Obj: TMyType): Pointer;")
                .appendImpl("begin")
                .appendImpl("  Result := Pointer(Obj);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testCastToSelfTypedPointerShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NonLinearCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyType = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  PMyType = ^TMyType;")
                .appendImpl("function AsPointer(Obj: TMyType): PMyType;")
                .appendImpl("begin")
                .appendImpl("  Result := PMyType(Obj);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testCastToParentTypedPointerShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NonLinearCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TParent = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TParent)")
                .appendDecl("  end;")
                .appendDecl("  PParent = ^TParent;")
                .appendImpl("function AsPointer(Obj: TChild): PParent;")
                .appendImpl("begin")
                .appendImpl("  Result := PParent(Obj);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testCastToChildTypedPointerShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NonLinearCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TParent = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TParent)")
                .appendDecl("  end;")
                .appendDecl("  PChild = ^TChild;")
                .appendImpl("function AsPointer(Obj: TParent): PChild;")
                .appendImpl("begin")
                .appendImpl("  Result := PChild(Obj);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testCastToUnrelatedPointerShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NonLinearCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TDog = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  TCat = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  PCat = ^TCat;")
                .appendImpl("function AsPointer(Obj: TDog): PCat;")
                .appendImpl("begin")
                .appendImpl("  Result := PCat(Obj); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
