/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.pmd.rules;

import static au.com.integradev.delphi.pmd.DelphiPmdConstants.TEMPLATE_XPATH_CLASS;
import static au.com.integradev.delphi.pmd.DelphiPmdConstants.TEMPLATE_XPATH_EXPRESSION_PARAM;
import static au.com.integradev.delphi.utils.conditions.RuleKey.ruleKey;
import static au.com.integradev.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.pmd.xml.DelphiRule;
import au.com.integradev.delphi.pmd.xml.DelphiRuleProperty;
import au.com.integradev.delphi.utils.builders.DelphiTestUnitBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XPathRuleTest extends BasePmdRuleTest {
  private DelphiRuleProperty xPathProperty;

  @BeforeEach
  void setup() {
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
  void testTypeIsFunctionShouldAddIssueForExactMatch() {
    xPathProperty.setValue("//TypeDeclarationNode[typeIs('Test.TFoo')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("XPathTestRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testTypeIsFunctionShouldAddIssueForSubTypeExactMatch() {
    xPathProperty.setValue("//TypeDeclarationNode[typeIs('System.TObject')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("XPathTestRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testTypeIsFunctionShouldNotAddIssueForUnrelatedType() {
    xPathProperty.setValue("//TypeDeclarationNode[typeIs('Bars.TBar')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("XPathTestRule"));
  }

  @Test
  void testTypeIsFunctionUnexpectedArgumentsShouldFail() {
    xPathProperty.setValue("//TypeDeclarationNode[typeIs('Test.TFoo', 'BadArgument')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("XPathTestRule"));
  }

  @Test
  void testTypeIsFunctionOnUntypedNodeShouldFail() {
    xPathProperty.setValue("//CommonDelphiNode[typeIs('Test.TFoo')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("XPathTestRule"));
  }

  @Test
  void testTypeIsExactlyFunctionShouldAddIssue() {
    xPathProperty.setValue("//TypeDeclarationNode[typeIsExactly('Test.TFoo')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("XPathTestRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testTypeIsExactlyFunctionUnexpectedArgumentsShouldFail() {
    xPathProperty.setValue("//TypeDeclarationNode[typeIsExactly('Test.TFoo', 'BadArgument')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("XPathTestRule"));
  }

  @Test
  void testTypeIsExactlyFunctionOnUntypedNodeShouldFail() {
    xPathProperty.setValue("//CommonDelphiNode[typeIsExactly('Test.TFoo')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("XPathTestRule"));
  }

  @Test
  void testTypeInheritsFromShouldAddIssue() {
    xPathProperty.setValue("//TypeDeclarationNode[typeInheritsFrom('System.TObject')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("XPathTestRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testTypeInheritsFromFunctionUnexpectedArgumentsShouldFail() {
    xPathProperty.setValue("//TypeDeclarationNode[typeInheritsFrom('Test.TFoo', 'BadArgument')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("XPathTestRule"));
  }

  @Test
  void testTypeInheritsFromFunctionOnUntypedNodeShouldFail() {
    xPathProperty.setValue("//CommonDelphiNode[typeInheritsFrom('Test.TFoo')]");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("XPathTestRule"));
  }
}
