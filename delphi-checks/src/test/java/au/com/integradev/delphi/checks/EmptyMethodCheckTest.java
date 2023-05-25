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

import static au.com.integradev.delphi.conditions.RuleKey.ruleKey;
import static au.com.integradev.delphi.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.CheckTest;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import org.junit.jupiter.api.Test;

class EmptyMethodCheckTest extends CheckTest {

  @Test
  void testValidRule() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TEmptyProcs = class(TObject)")
            .appendDecl("  public")
            .appendDecl("    procedure One;")
            .appendDecl("    procedure Two;")
            .appendDecl("    procedure Three;")
            .appendDecl("  end;")
            .appendImpl("procedure TEmptyProcs.One;")
            .appendImpl("begin")
            .appendImpl("  WriteLn('OK');")
            .appendImpl("end;")
            .appendImpl("procedure TEmptyProcs.Two;")
            .appendImpl("begin")
            .appendImpl("  WriteLn('OK');")
            .appendImpl("end;")
            .appendImpl("procedure TEmptyProcs.Three;")
            .appendImpl("begin")
            .appendImpl("  WriteLn('OK');")
            .appendImpl("end;")
            .appendImpl("procedure GlobalProcedureFour;")
            .appendImpl("begin")
            .appendImpl("  WriteLn('OK');")
            .appendImpl("end;")
            .appendImpl("procedure TNonexistentType.ProcedureFive;")
            .appendImpl("begin")
            .appendImpl("  WriteLn('OK');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyMethodRule"));
  }

  @Test
  void testEmptyMethods() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TEmptyProcs = class(TObject)")
            .appendDecl("  public")
            .appendDecl("    procedure One;")
            .appendDecl("    procedure Two;")
            .appendDecl("    procedure Three;")
            .appendDecl("    procedure Four;")
            .appendDecl("    procedure Five;")
            .appendDecl("  end;")
            .appendImpl("procedure TEmptyProcs.One;")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;")
            .appendImpl("procedure TEmptyProcs.Two;")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;")
            .appendImpl("procedure TEmptyProcs.Three;")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;")
            .appendImpl("procedure GlobalProcedureFour;")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;")
            .appendImpl("procedure TNonexistentType.ProcedureFive;")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("EmptyMethodRule", builder.getOffset() + 1))
        .areExactly(1, ruleKeyAtLine("EmptyMethodRule", builder.getOffset() + 5))
        .areExactly(1, ruleKeyAtLine("EmptyMethodRule", builder.getOffset() + 9))
        .areExactly(1, ruleKeyAtLine("EmptyMethodRule", builder.getOffset() + 13))
        .areExactly(1, ruleKeyAtLine("EmptyMethodRule", builder.getOffset() + 17));
  }

  @Test
  void testEmptyExceptionalMethodsWithoutComments() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TEmptyProcs = class(TObject)")
            .appendDecl("    type")
            .appendDecl("      TNestedType<T> = class(TNestedTypeBase)")
            .appendDecl("        public")
            .appendDecl("          procedure NestedOverride<T>; override;")
            .appendDecl("      end;")
            .appendDecl("  public")
            .appendDecl("    procedure OverrideProc; override;")
            .appendDecl("    procedure VirtualProc; virtual;")
            .appendDecl("  end;")
            .appendImpl("procedure TEmptyProcs.OverrideProc;")
            .appendImpl("begin")
            .appendImpl("end;")
            .appendImpl("procedure TEmptyProcs.VirtualProc;")
            .appendImpl("begin")
            .appendImpl("end;")
            .appendImpl("procedure TEmptyProcs.TTestedType<T>.NestedOverride<T>;")
            .appendImpl("begin")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("EmptyMethodRule", builder.getOffset() + 1))
        .areExactly(1, ruleKeyAtLine("EmptyMethodRule", builder.getOffset() + 4))
        .areExactly(1, ruleKeyAtLine("EmptyMethodRule", builder.getOffset() + 7));
  }

  @Test
  void testEmptyExceptionalMethodsWithComments() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TEmptyProcs = class(TObject)")
            .appendDecl("    type")
            .appendDecl("      TNestedType<T> = class(TNestedTypeBase)")
            .appendDecl("        public")
            .appendDecl("          procedure NestedOverride<T>; override;")
            .appendDecl("      end;")
            .appendDecl("  public")
            .appendDecl("    procedure OverrideProc; override;")
            .appendDecl("    procedure VirtualProc; virtual;")
            .appendDecl("  end;")
            .appendImpl("procedure TEmptyProcs.OverrideProc;")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;")
            .appendImpl("procedure TEmptyProcs.VirtualProc;")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;")
            .appendImpl("procedure TEmptyProcs.TNestedType<T>.NestedOverride<T>;")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyMethodRule"));
  }

  @Test
  void testFalsePositiveForwardTypeDeclaration() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TEmptyProcs = class; // forward declaration")
            .appendDecl("  TEmptyProcs = class(TObject)")
            .appendDecl("  public")
            .appendDecl("    procedure VirtualProc; virtual;")
            .appendDecl("  end;")
            .appendImpl("procedure TEmptyProcs.VirtualProc;")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyMethodRule"));
  }

  @Test
  void testFalsePositiveOverloadedMethod() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TEmptyProcs = class(TObject)")
            .appendDecl("  public")
            .appendDecl("    procedure VirtualProc(MyArg: String; MyOtherArg: Boolean); overload;")
            .appendDecl("    procedure VirtualProc(Arg1: String; Arg2: String); overload; virtual;")
            .appendDecl("  end;")
            .appendImpl("procedure TEmptyProcs.VirtualProc(FirstName: String; LastName: String);")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyMethodRule"));
  }

  @Test
  void testForwardDeclarationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure ForwardProc(FirstName: String; LastName: String); forward;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyMethodRule"));
  }

  @Test
  void testExternalImplementationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure ExternalProc(FirstName: String; LastName: String); external;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyMethodRule"));
  }

  @Test
  void testInterfaceImplementationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  IFoo = interface")
            .appendDecl("    procedure Bar;")
            .appendDecl("  end;")
            .appendDecl("  TFoo = class(TObject, IFoo)")
            .appendDecl("    procedure Bar;")
            .appendDecl("  end;")
            .appendImpl("procedure TFoo.Bar;")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyMethodRule"));
  }
}
