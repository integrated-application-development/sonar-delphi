package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class MixedNamesRuleTest extends BasePmdRuleTest {

  @Test
  public void testMatchingVarNamesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  MyVar: Boolean;")
            .appendImpl("begin")
            .appendImpl("  MyVar := True;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testMismatchedVarNamesShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  MyVar: Boolean;")
            .appendImpl("begin")
            .appendImpl("  myvar := True;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("MixedNamesRule", builder.getOffset() + 5));
  }

  @Test
  public void testQualifiedVarNamesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  MyVar: Boolean;")
            .appendImpl("begin")
            .appendImpl("  FMyField.myvar := True;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testMatchingFunctionNamesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class(TObject)")
            .appendDecl("  procedure DoThing(SomeArg: ArgType);")
            .appendDecl("end;")
            .appendImpl("procedure TFoo.DoThing(SomeArg: ArgType);")
            .appendImpl("begin")
            .appendImpl("  DoAnotherThing(SomeArg);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testMismatchedTypeNameShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class(TObject)")
            .appendDecl("  procedure DoThing(SomeArg: ArgType);")
            .appendDecl("end;")
            .appendImpl("procedure Tfoo.DoThing(SomeArg: ArgType);")
            .appendImpl("begin")
            .appendImpl("  DoAnotherThing(SomeArg);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("MixedNamesRule", builder.getOffset() + 1));
  }

  @Test
  public void testMismatchedFunctionNameShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class(TObject)")
            .appendDecl("  procedure DoThing(SomeArg: ArgType);")
            .appendDecl("end;")
            .appendImpl("procedure TFoo.doThing(SomeArg: ArgType);")
            .appendImpl("begin")
            .appendImpl("  DoAnotherThing(SomeArg);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("MixedNamesRule", builder.getOffset() + 1));
  }

  @Test
  public void testMismatchedExceptionNameShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    raise Exception.Create('Everything is on fire!');")
            .appendImpl("  except")
            .appendImpl("    on E: Exception do begin")
            .appendImpl("      e.Bar;")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("MixedNamesRule", builder.getOffset() + 7));
  }

  @Test
  public void testMismatchedVarNameInAsmBlockShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure; forward;")
            .appendImpl("procedure MyProcedure;")
            .appendImpl("var")
            .appendImpl("  MyArg: Integer;")
            .appendImpl("begin")
            .appendImpl("  asm")
            .appendImpl("    MOV EAX, Myarg")
            .appendImpl("    ADD EAX, 2")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testMismatchedVarNameInAsmProcShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure; forward;")
            .appendImpl("procedure MyProcedure;")
            .appendImpl("var")
            .appendImpl("  MyArg: Integer;")
            .appendImpl("asm")
            .appendImpl("  MOV EAX, Myarg")
            .appendImpl("  ADD EAX, 2")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testSelfShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    procedure Bar;")
            .appendDecl("  end;")
            .appendImpl("procedure TFoo.Bar;")
            .appendImpl("begin")
            .appendImpl("  Self.Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testPrimaryExpressionNameResolverBugShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    class procedure Finalise;")
            .appendDecl("  end;")
            .appendImpl("class procedure TFoo.Finalise;")
            .appendImpl("begin")
            .appendImpl("  TFoo(UnknownObject).Finalise;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
