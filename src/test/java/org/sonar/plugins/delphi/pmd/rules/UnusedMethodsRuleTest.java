package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class UnusedMethodsRuleTest extends BasePmdRuleTest {
  @Test
  void testUnusedMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("UnusedMethodsRule", builder.getOffset() + 1));
  }

  @Test
  void testUnusedRecursiveMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  Foo;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("UnusedMethodsRule", builder.getOffset() + 1));
  }

  @Test
  void testUsedMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;")
            .appendImpl("initialization")
            .appendImpl("  Foo;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedMethodsRule"));
  }

  @Test
  void testUnusedDeclaredMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Foo;")
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("UnusedMethodsRule", builder.getOffsetDecl() + 1))
        .areNot(ruleKeyAtLine("UnusedMethodsRule", builder.getOffset() + 1));
  }

  @Test
  void testUnusedRecursiveDeclaredMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Foo;")
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  Foo;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("UnusedMethodsRule", builder.getOffsetDecl() + 1))
        .areNot(ruleKeyAtLine("UnusedMethodsRule", builder.getOffset() + 1));
  }

  @Test
  void testUnusedMemberMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  public")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;")
            .appendImpl("procedure TFoo.Foo;")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("UnusedMethodsRule", builder.getOffsetDecl() + 4));
  }

  @Test
  void testUnusedRecursiveMemberMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  public")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;")
            .appendImpl("procedure TFoo.Foo;")
            .appendImpl("begin")
            .appendImpl("  Foo;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("UnusedMethodsRule", builder.getOffsetDecl() + 4));
  }

  @Test
  void testUnusedConstructorShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  public")
            .appendDecl("    constructor Create;")
            .appendDecl("  end;")
            .appendImpl("constructor TFoo.Create;")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("UnusedMethodsRule", builder.getOffsetDecl() + 4));
  }

  @Test
  void testUnusedConstructorWithMissingImplementationShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  public")
            .appendDecl("    constructor Create;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("UnusedMethodsRule", builder.getOffsetDecl() + 4));
  }

  @Test
  void testUnusedForbiddenConstructorShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  public")
            .appendDecl("    constructor Create;")
            .appendDecl("  end;")
            .appendImpl("constructor TFoo.Create;")
            .appendImpl("begin")
            .appendImpl("  raise Exception.Create('Do not use this constructor.');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedMethodsRule"));
  }

  @Test
  void testUsedMemberMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  public")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;")
            .appendImpl("procedure TFoo.Foo;")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;")
            .appendImpl("var")
            .appendImpl("  F: TFoo;")
            .appendImpl("initialization")
            .appendImpl("  F.Foo;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedMethodsRule"));
  }

  @Test
  void testUnusedOverrideMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  public")
            .appendDecl("    procedure Foo; override;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedMethodsRule"));
  }

  @Test
  void testMessageMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  public")
            .appendDecl("    procedure Foo(var Message: TMessage); message 123;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedMethodsRule"));
  }

  @Test
  void testNonCallableMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  public")
            .appendDecl("    class constructor Create;")
            .appendDecl("    class destructor Destroy;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedMethodsRule"));
  }

  @Test
  void testRegisterMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Register;")
            .appendImpl("procedure Register;")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedMethodsRule"));
  }

  @Test
  void testUnusedInterfaceImplementationMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  IFoo = interface")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;")
            .appendDecl("  TFoo = class(TInterfacedObject, IFoo)")
            .appendDecl("  public")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedMethodsRule"));
  }
}
