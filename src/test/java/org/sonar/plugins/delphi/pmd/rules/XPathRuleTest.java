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
  public void testTypeIsFunctionShouldAddIssueForExactMatch() {
    xPathProperty.setValue("//TypeDeclarationNode[typeIs('Test.TFoo')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("XPathTestRule", builder.getOffsetDecl() + 2));
  }

  @Test
  public void testTypeIsFunctionShouldAddIssueForSubTypeExactMatch() {
    xPathProperty.setValue("//TypeDeclarationNode[typeIs('System.TObject')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("XPathTestRule", builder.getOffsetDecl() + 2));
  }

  @Test
  public void testTypeIsFunctionShouldNotAddIssueForUnrelatedType() {
    xPathProperty.setValue("//TypeDeclarationNode[typeIs('Bars.TBar')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testTypeIsFunctionUnexpectedArgumentsShouldFail() {
    xPathProperty.setValue("//TypeDeclarationNode[typeIs('Test.TFoo', 'BadArgument')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testTypeIsFunctionOnUntypedNodeShouldFail() {
    xPathProperty.setValue("//CommonDelphiNode[typeIs('Test.TFoo')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testTypeIsExactlyFunctionShouldAddIssue() {
    xPathProperty.setValue("//TypeDeclarationNode[typeIsExactly('Test.TFoo')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
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
    xPathProperty.setValue("//TypeDeclarationNode[typeIsExactly('Test.TFoo', 'BadArgument')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testTypeIsExactlyFunctionOnUntypedNodeShouldFail() {
    xPathProperty.setValue("//CommonDelphiNode[typeIsExactly('Test.TFoo')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
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
    xPathProperty.setValue("//TypeDeclarationNode[typeInheritsFrom('Test.TFoo', 'BadArgument')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testTypeInheritsFromFunctionOnUntypedNodeShouldFail() {
    xPathProperty.setValue("//CommonDelphiNode[typeInheritsFrom('Test.TFoo')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
