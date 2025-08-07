/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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

class ObjectPassedAsInterfaceCheckTest {
  @Test
  void testObjectPassedAsObjectShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ObjectPassedAsInterfaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IFooIntf = interface")
                .appendDecl("  end;")
                .appendDecl("  TFooImpl = class(TInterfacedObject, IFooIntf)")
                .appendDecl("  end;")
                .appendDecl("procedure DoThing(Obj: TFooImpl);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Obj: TFooImpl;")
                .appendImpl("begin")
                .appendImpl("  Obj := TFooImpl.Create;")
                .appendImpl("  DoThing(Obj);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testObjectPassedAsInterfaceShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ObjectPassedAsInterfaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IFooIntf = interface")
                .appendDecl("  end;")
                .appendDecl("  TFooImpl = class(TInterfacedObject, IFooIntf)")
                .appendDecl("  end;")
                .appendDecl("procedure DoThing(Obj: IFooIntf);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Obj: TFooImpl;")
                .appendImpl("begin")
                .appendImpl("  Obj := TFooImpl.Create;")
                .appendImpl("  DoThing(Obj); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testQualifiedObjectPassedAsInterfaceShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ObjectPassedAsInterfaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TInterfacedObject, IInterface)")
                .appendDecl("  end;")
                .appendDecl("  IBar = interface")
                .appendDecl("    property Foo: TFoo;")
                .appendDecl("  end;")
                .appendDecl("  TBar = class(TInterfacedObject, IBar)")
                .appendDecl("  end;")
                .appendDecl("procedure DoThing(Obj: IInterface);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Intf: IBar;")
                .appendImpl("begin")
                .appendImpl("  Intf := TBar.Create;")
                .appendImpl("  DoThing(Intf.Foo); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testComplexQualifiedObjectPassedAsInterfaceShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ObjectPassedAsInterfaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TInterfacedObject, IInterface)")
                .appendDecl("  end;")
                .appendDecl("  IBar = interface")
                .appendDecl("    property Foo: TFoo;")
                .appendDecl("  end;")
                .appendDecl("  TBar = class(TInterfacedObject, IBar)")
                .appendDecl("  end;")
                .appendDecl("procedure DoThing(Obj: IInterface);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Intfs: TArray<IBar>;")
                .appendImpl("begin")
                .appendImpl("  Intfs := [TBar.Create];")
                .appendImpl("  DoThing(Intfs[0].Foo); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testInterfacePassedAsInterfaceShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ObjectPassedAsInterfaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure DoThing(Intf: IInterface);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Intf: IInterface;")
                .appendImpl("begin")
                .appendImpl("  Intf := TInterfacedObject.Create;")
                .appendImpl("  DoThing(Intf);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testQualifiedInterfacePassedAsInterfaceShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ObjectPassedAsInterfaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("    property Intf: IInterface;")
                .appendDecl("  end;")
                .appendDecl("procedure DoThing(Intf: IInterface);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: TFoo;")
                .appendImpl("begin")
                .appendImpl("  Foo := TFoo.Create;")
                .appendImpl("  DoThing(Foo.Intf);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testComplexQualifiedInterfacePassedAsInterfaceShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ObjectPassedAsInterfaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("    property Intf: IInterface;")
                .appendDecl("  end;")
                .appendDecl("procedure DoThing(Intf: IInterface);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foos: TArray<TFoo>;")
                .appendImpl("begin")
                .appendImpl("  Foos := [TFoo.Create];")
                .appendImpl("  DoThing(Foos[0].Intf);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testObjectCastToInterfaceShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ObjectPassedAsInterfaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IFooIntf = interface")
                .appendDecl("  end;")
                .appendDecl("  TFooImpl = class(TInterfacedObject, IFooIntf)")
                .appendDecl("  end;")
                .appendDecl("procedure DoThing(Obj: IFooIntf);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Obj: TFooImpl;")
                .appendImpl("begin")
                .appendImpl("  Obj := TFooImpl.Create;")
                .appendImpl("  DoThing(IFooIntf(Obj));")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNewObjectPassedAsInterfaceShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ObjectPassedAsInterfaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IFooIntf = interface")
                .appendDecl("  end;")
                .appendDecl("  TFooImpl = class(TInterfacedObject, IFooIntf)")
                .appendDecl("  end;")
                .appendDecl("procedure DoThing(Obj: IFooIntf);")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  DoThing(TFooImpl.Create);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testObjectPassedAsInterfaceToInheritedShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ObjectPassedAsInterfaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IFooIntf = interface")
                .appendDecl("  end;")
                .appendDecl("  TFooParent = class(TInterfacedObject)")
                .appendDecl("    procedure Bar(Foo: IFooIntf); virtual;")
                .appendDecl("  end;")
                .appendDecl("  TFooImpl = class(TFooParent, IFooIntf)")
                .appendDecl("    procedure Bar(Foo: IFooIntf); override;")
                .appendDecl("  end;")
                .appendImpl("procedure TFooImpl.Bar(Foo: IFooIntf);")
                .appendImpl("var")
                .appendImpl("  Obj: TFooImpl;")
                .appendImpl("begin")
                .appendImpl("  Obj := TFooImpl.Create;")
                .appendImpl("  inherited Bar(Obj); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testExcludedTypePassedAsInterfaceShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ObjectPassedAsInterfaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  System.Classes;")
                .appendDecl("procedure DoThing(Obj: IInterface);")
                .appendImpl("procedure Test(Obj: TComponent);")
                .appendImpl("begin")
                .appendImpl("  DoThing(Obj);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testExcludedTypeDescendentPassedAsInterfaceShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ObjectPassedAsInterfaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  System.Classes;")
                .appendDecl("type")
                .appendDecl("  IFooIntf = interface")
                .appendDecl("  end;")
                .appendDecl("  TFooImpl = class(TComponent, IFooIntf)")
                .appendDecl("  end;")
                .appendDecl("procedure DoThing(Obj: IFooIntf);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Obj: TFooImpl;")
                .appendImpl("begin")
                .appendImpl("  Obj := TFooImpl.Create;")
                .appendImpl("  DoThing(Obj);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
