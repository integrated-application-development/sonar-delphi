package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class ExplicitDefaultPropertyReferenceRuleTest extends BasePmdRuleTest {
  @Test
  public void testImplicitDefaultPropertyAccessShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("    function GetBar: TObject;")
            .appendDecl("    property Bar: TObject read GetBar; default;")
            .appendDecl("  end;")
            .appendImpl("procedure Test(Foo: TFoo);")
            .appendImpl("var")
            .appendImpl("  Bar: TObject;")
            .appendImpl("begin")
            .appendImpl("  Bar := Foo[0];")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ExplicitDefaultPropertyReferenceRule"));
  }

  @Test
  public void testExplicitDefaultPropertyAccessShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("    function GetBar: TObject;")
            .appendDecl("    property Bar[Index: Integer]: TObject read GetBar; default;")
            .appendDecl("  end;")
            .appendImpl("procedure Test(Foo: TFoo);")
            .appendImpl("var")
            .appendImpl("  Bar: TObject;")
            .appendImpl("begin")
            .appendImpl("  Bar := Foo.Bar[0];")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(
            1, ruleKeyAtLine("ExplicitDefaultPropertyReferenceRule", builder.getOffset() + 5));
  }

  @Test
  public void testExplicitDefaultPropertyAccessOnSelfShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("    function GetBar: TObject;")
            .appendDecl("    procedure Test(Foo: TFoo);")
            .appendDecl("    property Bar[Index: Integer]: TObject read GetBar; default;")
            .appendDecl("  end;")
            .appendImpl("procedure TFoo.Test(Foo: TFoo);")
            .appendImpl("var")
            .appendImpl("  Obj: TObject;")
            .appendImpl("begin")
            .appendImpl("  Obj := Bar[0];")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ExplicitDefaultPropertyReferenceRule"));
  }
}
