package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.TEMPLATE_XPATH_CLASS;
import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.TEMPLATE_XPATH_EXPRESSION_PARAM;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class XPathRuleTest extends BasePmdRuleTest {
  private DelphiRuleProperty xPathProperty;

  @Before
  public void setup() {
    DelphiRule rule = new DelphiRule();
    xPathProperty = new DelphiRuleProperty(TEMPLATE_XPATH_EXPRESSION_PARAM, "");
    rule.setName("XPathTestRule");
    rule.setTemplateName("XPathTemplateRule");
    rule.setPriority(5);
    rule.setClazz(TEMPLATE_XPATH_CLASS);
    rule.addProperty(xPathProperty);
    addRule(rule);
  }

  @Test
  public void testTypeIsFunctionShouldAddIssue() {
    xPathProperty.setValue("//TypeDeclarationNode[typeIs('TestUnits.TFoo')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName("TestUnits")
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("XPathTestRule", builder.getOffsetDecl() + 2));
  }

  @Test
  public void testTypeIsFunctionUnexpectedArgumentsShouldFail() {
    xPathProperty.setValue("//TypeDeclarationNode[typeIs('TestUnits.TFoo', 'BadArgument')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName("TestUnits")
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testTypeIsFunctionOnUntypedNodeShouldFail() {
    xPathProperty.setValue("//CommonDelphiNode[typeIs('TestUnits.TFoo')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName("TestUnits")
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testTypeIsExactlyFunctionShouldAddIssue() {
    xPathProperty.setValue("//TypeDeclarationNode[typeIsExactly('TestUnits.TFoo')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName("TestUnits")
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("XPathTestRule", builder.getOffsetDecl() + 2));
  }

  @Test
  public void testTypeIsExactlyFunctionUnexpectedArgumentsShouldFail() {
    xPathProperty.setValue(
        "//TypeDeclarationNode[typeIsExactly('TestUnits.TFoo', 'BadArgument')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName("TestUnits")
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testTypeIsExactlyFunctionOnUntypedNodeShouldFail() {
    xPathProperty.setValue("//CommonDelphiNode[typeIsExactly('TestUnits.TFoo')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName("TestUnits")
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testTypeInheritsFromShouldAddIssue() {
    xPathProperty.setValue("//TypeDeclarationNode[typeInheritsFrom('System.TObject')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName("TestUnits")
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("XPathTestRule", builder.getOffsetDecl() + 2));
  }

  @Test
  public void testTypeInheritsFromFunctionUnexpectedArgumentsShouldFail() {
    xPathProperty.setValue(
        "//TypeDeclarationNode[typeInheritsFrom('TestUnits.TFoo', 'BadArgument')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName("TestUnits")
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testTypeInheritsFromFunctionOnUntypedNodeShouldFail() {
    xPathProperty.setValue("//CommonDelphiNode[typeInheritsFrom('TestUnits.TFoo')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName("TestUnits")
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
