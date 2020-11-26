package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class InheritedTypeNameRuleTest extends BasePmdRuleTest {

  private DelphiRuleProperty nameRegex;
  private DelphiRuleProperty parentRegex;

  @BeforeEach
  void setup() {
    nameRegex = new DelphiRuleProperty(InheritedTypeNameRule.NAME_REGEX.name(), ".*_Child");
    parentRegex = new DelphiRuleProperty(InheritedTypeNameRule.PARENT_REGEX.name(), ".*_Parent");

    DelphiRule rule = new DelphiRule();
    rule.setName("TestInheritedNameRule");
    rule.setClazz("org.sonar.plugins.delphi.pmd.rules.InheritedTypeNameRule");
    rule.setPriority(5);
    rule.addProperty(nameRegex);
    rule.addProperty(parentRegex);

    addRule(rule);
  }

  @Test
  void testCompliesWithNamingConventionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType_Child = class(TType_Parent)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testFailsNamingConventionShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(TType_Parent)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("TestInheritedNameRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testFailsNamingConventionWithMultipleParentsShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(IType, TType_Parent)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("TestInheritedNameRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testDoesNotInheritFromExpectedTypeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(TSomeOtherType)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testDoesNotInheritFromAnyTypeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(TObject)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testBadNameRegexShouldNotAddIssue() {
    nameRegex.setValue("*");

    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(TType_Parent)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testBadParentRegexShouldNotAddIssue() {
    parentRegex.setValue("*");

    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(TType_Parent)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
