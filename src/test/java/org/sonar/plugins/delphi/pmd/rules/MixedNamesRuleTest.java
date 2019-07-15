package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class MixedNamesRuleTest extends BasePmdRuleTest {

  @Test
  public void testMatchingVarNamesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure Test;");
    builder.appendImpl("var");
    builder.appendImpl("  MyVar: Boolean;");
    builder.appendImpl("begin");
    builder.appendImpl("  MyVar := True;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testMismatchedVarNamesShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure Test;");
    builder.appendImpl("var");
    builder.appendImpl("  MyVar: Boolean;");
    builder.appendImpl("begin");
    builder.appendImpl("  myvar := True;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("MixedNamesRule", builder.getOffSet() + 5)));
  }


  @Test
  public void testMatchingFunctionNamesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type TClass = class");
    builder.appendDecl("  procedure DoThing(SomeArg: ArgType);");
    builder.appendDecl("end;");

    builder.appendImpl("procedure TClass.DoThing(SomeArg: ArgType);");
    builder.appendImpl("begin");
    builder.appendImpl("  DoAnotherThing(SomeArg);");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testMismatchedTypeNameShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type TClass = class");
    builder.appendDecl("  procedure DoThing(SomeArg: ArgType);");
    builder.appendDecl("end;");

    builder.appendImpl("procedure Tclass.DoThing(SomeArg: ArgType);");
    builder.appendImpl("begin");
    builder.appendImpl("  DoAnotherThing(SomeArg);");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("MixedNamesRule", builder.getOffSet() + 1)));
  }

  @Test
  public void testMismatchedFunctionNameShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type TClass = class");
    builder.appendDecl("  procedure DoThing(SomeArg: ArgType);");
    builder.appendDecl("end;");

    builder.appendImpl("procedure TClass.doThing(SomeArg: ArgType);");
    builder.appendImpl("begin");
    builder.appendImpl("  DoAnotherThing(SomeArg);");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("MixedNamesRule", builder.getOffSet() + 1)));
  }

}
