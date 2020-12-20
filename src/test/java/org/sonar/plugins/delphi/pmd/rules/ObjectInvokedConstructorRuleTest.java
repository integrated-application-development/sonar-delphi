package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class ObjectInvokedConstructorRuleTest extends BasePmdRuleTest {

  @Test
  void testConstructorInvokedOnObjectShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Obj: TObject;")
            .appendImpl("begin")
            .appendImpl("  Obj.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ObjectInvokedConstructorRule", builder.getOffset() + 5));
  }

  @Test
  void testConstructorInvokedOnTypeIdentifierShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Obj: TObject;")
            .appendImpl("begin")
            .appendImpl("  Obj := TObject.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ObjectInvokedConstructorRule"));
  }

  @Test
  void testConstructorInvokedOnClassReferenceShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Obj: TObject;")
            .appendImpl("  Clazz: TClass;")
            .appendImpl("begin")
            .appendImpl("  Obj := Clazz.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ObjectInvokedConstructorRule"));
  }

  @Test
  void testConstructorInvokedOnSelfShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    constructor Create;")
            .appendDecl("    procedure Test;")
            .appendDecl("  end;")
            .appendImpl("procedure TFoo.Test;")
            .appendImpl("begin")
            .appendImpl("  Self.Create;")
            .appendImpl("  Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ObjectInvokedConstructorRule"));
  }

  @Test
  void testBareInheritedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    constructor Create;")
            .appendDecl("  end;")
            .appendImpl("constructor TFoo.Create;")
            .appendImpl("begin")
            .appendImpl("  inherited;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ObjectInvokedConstructorRule"));
  }

  @Test
  void testNamedInheritedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    constructor Create;")
            .appendDecl("  end;")
            .appendImpl("constructor TFoo.Create;")
            .appendImpl("begin")
            .appendImpl("  inherited Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ObjectInvokedConstructorRule"));
  }

  @Test
  void testQualifiedInheritedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    constructor Create;")
            .appendDecl("  end;")
            .appendImpl("constructor TFoo.Create;")
            .appendImpl("begin")
            .appendImpl("  inherited.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ObjectInvokedConstructorRule"));
  }
}
