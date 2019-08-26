package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.utils.matchers.IssueMatchers.hasRuleKeyAtLine;

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

    assertIssues(empty());
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

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("MixedNamesRule", builder.getOffSet() + 5)));
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

    assertIssues(empty());
  }

  @Test
  public void testMatchingFunctionNamesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TClass = class")
            .appendDecl("  procedure DoThing(SomeArg: ArgType);")
            .appendDecl("end;")
            .appendImpl("procedure TClass.DoThing(SomeArg: ArgType);")
            .appendImpl("begin")
            .appendImpl("  DoAnotherThing(SomeArg);")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testMismatchedTypeNameShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TClass = class")
            .appendDecl("  procedure DoThing(SomeArg: ArgType);")
            .appendDecl("end;")
            .appendImpl("procedure Tclass.DoThing(SomeArg: ArgType);")
            .appendImpl("begin")
            .appendImpl("  DoAnotherThing(SomeArg);")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("MixedNamesRule", builder.getOffSet() + 1)));
  }

  @Test
  public void testMismatchedFunctionNameShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TClass = class")
            .appendDecl("  procedure DoThing(SomeArg: ArgType);")
            .appendDecl("end;")
            .appendImpl("procedure TClass.doThing(SomeArg: ArgType);")
            .appendImpl("begin")
            .appendImpl("  DoAnotherThing(SomeArg);")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("MixedNamesRule", builder.getOffSet() + 1)));
  }

  @Test
  public void testMismatchedVarNameInAsmBlockShouldAddIssue() {
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

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("MixedNamesRule", builder.getOffSet() + 7)));
  }

  @Test
  public void testMismatchedVarNameInAsmProcShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure(MyArg: Integer); forward;")
            .appendImpl("procedure MyProcedure(MyArg: Integer);")
            .appendImpl("var")
            .appendImpl("  MyArg: Integer;")
            .appendImpl("asm")
            .appendImpl("  MOV EAX, Myarg")
            .appendImpl("  ADD EAX, 2")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("MixedNamesRule", builder.getOffSet() + 6)));
  }
}
