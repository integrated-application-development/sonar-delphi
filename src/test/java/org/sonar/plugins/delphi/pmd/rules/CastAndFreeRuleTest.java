package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class CastAndFreeRuleTest extends BasePmdRuleTest {

  @Test
  public void testRegularFreeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  Bar.Free;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testRegularFreeAndNilShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  FreeAndNil(Bar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testSoftCastFreeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  (Bar as Xyzzy).Free;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  public void testHardCastFreeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  Xyzzy(Bar).Free;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  public void testSoftCastFreeAndNilShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  FreeAndNil(Bar as Xyzzy);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  public void testHardCastFreeAndNilShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  FreeAndNil(Xyzzy(Bar));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  public void testNestedSoftCastFreeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  (((Bar as Xyzzy))).Free;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  public void testNestedHardCastFreeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  ((Xyzzy(Bar))).Free;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  public void testNestedSoftCastFreeAndNilShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  FreeAndNil(((Bar as Xyzzy)));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  public void testNestedHardCastFreeAndNilShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  FreeAndNil(((Xyzzy(Bar))));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
  }
}
