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
                .appendDecl("  TFooImpl = class(TObject, IFooIntf)")
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
                .appendDecl("  TFooImpl = class(TObject, IFooIntf)")
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
  void testObjectCastToInterfaceShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ObjectPassedAsInterfaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IFooIntf = interface")
                .appendDecl("  end;")
                .appendDecl("  TFooImpl = class(TObject, IFooIntf)")
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
                .appendDecl("  TFooImpl = class(TObject, IFooIntf)")
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
                .appendDecl("  TFooParent = class(TObject)")
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
}
