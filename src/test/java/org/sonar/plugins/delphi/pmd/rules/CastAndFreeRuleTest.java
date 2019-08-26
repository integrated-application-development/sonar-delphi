package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.utils.matchers.IssueMatchers.hasRuleKeyAtLine;

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

    assertIssues(empty());
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

    assertIssues(empty());
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

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("CastAndFreeRule", builder.getOffSet() + 3)));
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

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("CastAndFreeRule", builder.getOffSet() + 3)));
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

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("CastAndFreeRule", builder.getOffSet() + 3)));
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

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("CastAndFreeRule", builder.getOffSet() + 3)));
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

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("CastAndFreeRule", builder.getOffSet() + 3)));
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

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("CastAndFreeRule", builder.getOffSet() + 3)));
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

    assertIssues(hasItem(hasRuleKeyAtLine("CastAndFreeRule", builder.getOffSet() + 3)));
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

    assertIssues(hasItem(hasRuleKeyAtLine("CastAndFreeRule", builder.getOffSet() + 3)));
  }
}
