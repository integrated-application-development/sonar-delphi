package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class EmptyMethodRuleTest extends BasePmdRuleTest {

  @Test
  public void testValidRule() {
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

    assertIssues().isEmpty();
  }

  @Test
  public void testEmptyMethods() {
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
        .hasSize(5)
        .areExactly(1, ruleKeyAtLine("EmptyMethodRule", builder.getOffset() + 1))
        .areExactly(1, ruleKeyAtLine("EmptyMethodRule", builder.getOffset() + 5))
        .areExactly(1, ruleKeyAtLine("EmptyMethodRule", builder.getOffset() + 9))
        .areExactly(1, ruleKeyAtLine("EmptyMethodRule", builder.getOffset() + 13))
        .areExactly(1, ruleKeyAtLine("EmptyMethodRule", builder.getOffset() + 17));
  }

  @Test
  public void testEmptyExceptionalMethodsWithoutComments() {
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
        .hasSize(3)
        .areExactly(1, ruleKeyAtLine("EmptyMethodRule", builder.getOffset() + 1))
        .areExactly(1, ruleKeyAtLine("EmptyMethodRule", builder.getOffset() + 4))
        .areExactly(1, ruleKeyAtLine("EmptyMethodRule", builder.getOffset() + 7));
  }

  @Test
  public void testEmptyExceptionalMethodsWithComments() {
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

    assertIssues().isEmpty();
  }

  @Test
  public void testFalsePositiveForwardTypeDeclaration() {
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
  public void testFalsePositiveOverloadedMethod() {
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
  public void testForwardDeclarationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure ForwardProc(FirstName: String; LastName: String); forward;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyMethodRule"));
  }
}
