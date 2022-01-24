package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class AddressOfNestedMethodRuleTest extends BasePmdRuleTest {
  @Test
  void testAddressOfRegularMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("var")
            .appendDecl("  ProcVar: Pointer;")
            .appendDecl("function ProcMethod(ProcVar: Pointer);")
            .appendImpl("procedure RegularMethod(Str: String);")
            .appendImpl("begin")
            .appendImpl("  Exit;")
            .appendImpl("end;")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  ProcVar := @RegularMethod;")
            .appendImpl("  ProcMethod(@RegularMethod);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("AddressOfNestedMethodRule"));
  }

  @Test
  void testAddressOfVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("var")
            .appendDecl("  ProcVar: Pointer;")
            .appendDecl("function ProcMethod(Ptr: Pointer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Str: String;")
            .appendImpl("begin")
            .appendImpl("  ProcVar := @Str;")
            .appendImpl("  ProcMethod(@Str);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("AddressOfNestedMethodRule"));
  }

  @Test
  void testAddressOfLiteralShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("function ProcMethod(Ptr: Pointer);")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  ProcVar := @'foo';")
            .appendImpl("  ProcMethod(@'bar');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("AddressOfNestedMethodRule"));
  }

  @Test
  void testAddressOfNestedMethodResultShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("var")
            .appendDecl("  ProcVar: Pointer;")
            .appendDecl("function ProcMethod(ProcVar: Pointer);")
            .appendImpl("procedure Test;")
            .appendImpl("  function Nested(Str: String): String;")
            .appendImpl("  begin")
            .appendImpl("    Result := Str;")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  ProcVar := @Nested('');")
            .appendImpl("  ProcMethod(@Nested(''));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("AddressOfNestedMethodRule"));
  }

  @Test
  void testAddressOfNestedMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("var")
            .appendDecl("  ProcVar: Pointer;")
            .appendDecl("function ProcMethod(ProcVar: Pointer);")
            .appendImpl("procedure Test;")
            .appendImpl("  procedure Nested(Str: String);")
            .appendImpl("  begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  ProcVar := @Nested;")
            .appendImpl("  ProcMethod(@Nested);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("AddressOfNestedMethodRule", builder.getOffset() + 7))
        .areExactly(1, ruleKeyAtLine("AddressOfNestedMethodRule", builder.getOffset() + 8));
  }
}
