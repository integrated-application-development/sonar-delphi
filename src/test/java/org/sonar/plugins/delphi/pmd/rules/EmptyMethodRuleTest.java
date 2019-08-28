package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.sonar.plugins.delphi.utils.matchers.HasRuleKey.hasRuleKey;
import static org.sonar.plugins.delphi.utils.matchers.IssueMatchers.hasRuleKeyAtLine;

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
            .appendImpl("  Writeln('OK');")
            .appendImpl("end;")
            .appendImpl("procedure TEmptyProcs.Two;")
            .appendImpl("begin")
            .appendImpl("  Writeln('OK');")
            .appendImpl("end;")
            .appendImpl("procedure TEmptyProcs.Three;")
            .appendImpl("begin")
            .appendImpl("  Writeln('OK');")
            .appendImpl("end;")
            .appendImpl("procedure GlobalProcedureFour;")
            .appendImpl("begin")
            .appendImpl("  Writeln('OK');")
            .appendImpl("end;")
            .appendImpl("procedure TNonexistentType.ProcedureFive;")
            .appendImpl("begin")
            .appendImpl("  Writeln('OK');")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
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

    assertIssues(hasSize(5));
    assertIssues(hasItem(hasRuleKeyAtLine("EmptyMethodRule", builder.getOffSet() + 1)));
    assertIssues(hasItem(hasRuleKeyAtLine("EmptyMethodRule", builder.getOffSet() + 5)));
    assertIssues(hasItem(hasRuleKeyAtLine("EmptyMethodRule", builder.getOffSet() + 9)));
    assertIssues(hasItem(hasRuleKeyAtLine("EmptyMethodRule", builder.getOffSet() + 13)));
    assertIssues(hasItem(hasRuleKeyAtLine("EmptyMethodRule", builder.getOffSet() + 17)));
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

    assertIssues(hasSize(3));
    assertIssues(hasItem(hasRuleKeyAtLine("EmptyMethodRule", builder.getOffSet() + 1)));
    assertIssues(hasItem(hasRuleKeyAtLine("EmptyMethodRule", builder.getOffSet() + 4)));
    assertIssues(hasItem(hasRuleKeyAtLine("EmptyMethodRule", builder.getOffSet() + 7)));
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

    assertIssues(empty());
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

    assertIssues(not(hasItem(hasRuleKey("EmptyMethodRule"))));
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

    assertIssues(not(hasItem(hasRuleKey("EmptyMethodRule"))));
  }

  @Test
  public void testForwardDeclarationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure ForwardProc(FirstName: String; LastName: String); forward;");

    execute(builder);

    assertIssues(not(hasItem(hasRuleKey("EmptyMethodRule"))));
  }
}
