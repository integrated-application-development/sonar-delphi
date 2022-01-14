package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class UnusedPropertiesRuleTest extends BasePmdRuleTest {
  @Test
  void testUnusedPropertyShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class")
            .appendDecl("private")
            .appendDecl("  FBar: Integer;")
            .appendDecl("public")
            .appendDecl("  property Bar: Integer read FBar;")
            .appendDecl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("UnusedPropertiesRule", builder.getOffsetDecl() + 5));
  }

  @Test
  void testUsedInMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class")
            .appendDecl("private")
            .appendDecl("  FBar: Integer;")
            .appendDecl("public")
            .appendDecl("  property Bar: Integer read FBar;")
            .appendDecl("end;")
            .appendImpl("procedure Baz(Foo: TFoo);")
            .appendImpl("begin")
            .appendImpl("  Foo.Bar := 0;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedPropertiesRule"));
  }

  @Test
  void testUsedDefaultPropertyShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class")
            .appendDecl("public")
            .appendDecl("   property Items[Index: Integer]: TObject; default;")
            .appendDecl("end;")
            .appendImpl("function Baz(Foo: TFoo): TObject;")
            .appendImpl("begin")
            .appendImpl("  Result := Foo[0];")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedPropertiesRule"));
  }

  @Test
  void testUnusedPublishedPropertyShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class")
            .appendDecl("private")
            .appendDecl("  FBar: Integer;")
            .appendDecl("published")
            .appendDecl("  property Bar: Integer read FBar;")
            .appendDecl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedPropertiesRule"));
  }
}
