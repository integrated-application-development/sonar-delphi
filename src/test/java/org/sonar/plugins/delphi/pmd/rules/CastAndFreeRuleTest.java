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
  public void testPointerToObjectCastShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("  System.SysUtils;")
            .appendImpl("procedure Foo(Bar: Pointer);")
            .appendImpl("begin")
            .appendImpl("  (TObject(Bar)).Free;")
            .appendImpl("  FreeAndNil(TObject(Bar));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testUntypedToObjectCastShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("  System.SysUtils;")
            .appendImpl("procedure Foo(Bar);")
            .appendImpl("begin")
            .appendImpl("  (TObject(Bar)).Free;")
            .appendImpl("  FreeAndNil(TObject(Bar));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testWeirdPointerToObjectCastShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("  System.SysUtils;")
            .appendDecl("type")
            .appendDecl("  TList = class(TObject)")
            .appendDecl("  property Default[Index: Integer]: Pointer; default;")
            .appendDecl("end;")
            .appendImpl("procedure Foo(List: TList);")
            .appendImpl("begin")
            .appendImpl("  (TList(List[0])).Free;")
            .appendImpl("  FreeAndNil(TObject(List[0]));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testWeirdSoftCastFreeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TXyzz = class(TObject) end;")
            .appendImpl("procedure Foo(Bar: Baz);")
            .appendImpl("begin")
            .appendImpl("  (Bar as TXyzz).Free;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
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

    assertIssues().areNot(ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
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
            .appendImpl("  TObject(Bar).Free;")
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
            .appendImpl("  FreeAndNil(Bar as TObject);")
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
            .appendImpl("  FreeAndNil(TObject(Bar));")
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
            .appendImpl("  (((Bar as TObject))).Free;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
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

    assertIssues().areExactly(1, ruleKeyAtLine("CastAndFreeRule", builder.getOffset() + 3));
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
