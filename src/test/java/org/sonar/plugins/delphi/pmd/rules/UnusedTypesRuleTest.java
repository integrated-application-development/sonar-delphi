package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class UnusedTypesRuleTest extends BasePmdRuleTest {
  @Test
  void testUnusedTypeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("UnusedTypesRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testUsedByFieldShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("    Bar: TFoo;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("UnusedTypesRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testUsedInMemberMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("    procedure Baz;")
            .appendDecl("  end;")
            .appendImpl("procedure TFoo.Baz;")
            .appendImpl("begin")
            .appendImpl("  var Foo: TFoo;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("UnusedTypesRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testUsedInMemberMethodParametersShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("    procedure Baz(Foo: TFoo);")
            .appendDecl("  end;")
            .appendImpl("procedure TFoo.Baz(Foo: TFoo);")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("UnusedTypesRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testUsedInMethodParametersShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  end;")
            .appendImpl("procedure Baz(Foo: TFoo);")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedTypesRule"));
  }

  @Test
  void testUsedInMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  end;")
            .appendImpl("procedure Baz;")
            .appendImpl("begin")
            .appendImpl("  var Foo: TFoo;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedTypesRule"));
  }

  @Test
  void testHelperShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  end;")
            .appendDecl("  TFooHelper = class helper for TFoo")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("UnusedTypesRule", builder.getOffsetDecl() + 4));
  }
}
