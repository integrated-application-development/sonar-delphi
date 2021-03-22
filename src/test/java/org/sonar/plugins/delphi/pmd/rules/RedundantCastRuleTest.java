package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class RedundantCastRuleTest extends BasePmdRuleTest {

  @Test
  void testNoCastShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  end;")
            .appendDecl("  TBar = class (TFoo)")
            .appendDecl("  end;")
            .appendImpl("procedure Foo(Bar: TBar);")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo := Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("RedundantCastRule"));
  }

  @Test
  void testRequiredCastShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  end;")
            .appendDecl("  TBar = class (TFoo)")
            .appendDecl("  end;")
            .appendImpl("procedure Foo(Foo: TFoo);")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Bar := TBar(Foo);")
            .appendImpl("  Bar := Foo as TBar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("RedundantCastRule"));
  }

  @Test
  void testRedundantCastShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  end;")
            .appendImpl("procedure Foo(Foo: TFoo);")
            .appendImpl("var")
            .appendImpl("  Foo2: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo2 := TFoo(Foo);")
            .appendImpl("  Foo2 := Foo as TFoo;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 5))
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 6));
  }

  @Test
  void testRedundantCastWithConstructorShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  end;")
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo := TFoo(TFoo.Create);")
            .appendImpl("  Foo := TFoo.Create as TFoo;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 5))
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 6));
  }

  @Test
  void testRedundantCastWithStringKeywordShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Arg: String);")
            .appendImpl("var")
            .appendImpl("  Str: String;")
            .appendImpl("begin")
            .appendImpl("  Str := String(Arg);")
            .appendImpl("  Str := Arg as String;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 5))
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 6));
  }

  @Test
  void testRedundantCastWithFileKeywordShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Arg: file);")
            .appendImpl("var")
            .appendImpl("  FileVar: file;")
            .appendImpl("begin")
            .appendImpl("  FileVar := file(Arg);")
            .appendImpl("  FileVar := Arg as file;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 5))
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 6));
  }

  @Test
  void testRedundantCastWithAmbiguousMethodCallShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("function GetString: String;")
            .appendImpl("procedure Foo(Arg: String);")
            .appendImpl("var")
            .appendImpl("  Str: String;")
            .appendImpl("begin")
            .appendImpl("  Str := String(GetString);")
            .appendImpl("  Str := GetString as String;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 5))
        .areExactly(1, ruleKeyAtLine("RedundantCastRule", builder.getOffset() + 6));
  }

  @Test
  void testUnknownTypesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("  end;")
            .appendImpl("function Foo(Foo: TFoo): TFoo;")
            .appendImpl("var")
            .appendImpl("  Unknown: TUnknown;")
            .appendImpl("begin")
            .appendImpl("  Result := TBar(Unknown);")
            .appendImpl("  Result := Foo as TUnknown;")
            .appendImpl("  Result := TUnknown(Foo);")
            .appendImpl("  Result := Unknown as TFoo;")
            .appendImpl("  Result := Unknown as TUnknown;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("RedundantCastRule"));
  }
}
