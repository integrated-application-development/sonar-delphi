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
package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.pmd.rules.ForbiddenImportFilePatternRule.FORBIDDEN_IMPORT_PATTERN;
import static org.sonar.plugins.delphi.pmd.rules.ForbiddenImportFilePatternRule.FORBIDDEN_IMPORT_SYNTAX;
import static org.sonar.plugins.delphi.pmd.rules.ForbiddenImportFilePatternRule.WHITELIST_PATTERN;
import static org.sonar.plugins.delphi.pmd.rules.ForbiddenImportFilePatternRule.WHITELIST_SYNTAX;
import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.utils.PathUtils;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class ForbiddenImportFilePatternRuleTest extends BasePmdRuleTest {
  private DelphiRuleProperty forbiddenPattern;
  private DelphiRuleProperty forbiddenSyntax;
  private DelphiRuleProperty whitelistPattern;
  private DelphiRuleProperty whitelistSyntax;

  @BeforeEach
  void setup() {
    DelphiRule rule = new DelphiRule();
    forbiddenPattern = new DelphiRuleProperty(FORBIDDEN_IMPORT_PATTERN.name());
    forbiddenSyntax = new DelphiRuleProperty(FORBIDDEN_IMPORT_SYNTAX.name());
    whitelistPattern = new DelphiRuleProperty(WHITELIST_PATTERN.name());
    whitelistSyntax = new DelphiRuleProperty(WHITELIST_SYNTAX.name());

    rule.setName("ForbiddenImportFilePatternRuleTest");
    rule.setTemplateName("ForbiddenImportFilePatternRule");
    rule.setPriority(5);
    rule.addProperty(forbiddenPattern);
    rule.addProperty(forbiddenSyntax);
    rule.addProperty(whitelistPattern);
    rule.addProperty(whitelistSyntax);
    rule.setClazz("org.sonar.plugins.delphi.pmd.rules.ForbiddenImportFilePatternRule");

    addRule(rule);
  }

  @Test
  void testForbiddenImportShouldAddIssue() {
    forbiddenPattern.setValue("**" + STANDARD_LIBRARY + "/System.SysUtils.pas");
    forbiddenSyntax.setValue("GLOB");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    Vcl.Controls")
            .appendDecl("  , System.SysUtils")
            .appendDecl("  ;");

    execute(builder);

    assertIssues()
        .areExactly(
            1, ruleKeyAtLine("ForbiddenImportFilePatternRuleTest", builder.getOffsetDecl() + 3));
  }

  @Test
  void testForbiddenImportInWhitelistedFileShouldNotAddIssue() {
    forbiddenPattern.setValue("**" + STANDARD_LIBRARY + "/System.SysUtils.pas");
    forbiddenSyntax.setValue("GLOB");
    whitelistPattern.setValue(PathUtils.sanitize(ROOT_DIR.getAbsolutePath()) + "/**");
    whitelistSyntax.setValue("GLOB");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    System.SysUtils")
            .appendDecl("  ;");

    execute(builder);

    assertIssues().areNot(ruleKey("ForbiddenImportFilePatternRuleTest"));
  }

  @Test
  void testInvalidPatternShouldNotAddIssue() {
    forbiddenPattern.setValue("**" + STANDARD_LIBRARY + "/System.SysUtils.pas");
    forbiddenSyntax.setValue("GLOB");
    whitelistPattern.setValue("[");
    whitelistSyntax.setValue("REGEX");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    System.SysUtils")
            .appendDecl("  ;");

    execute(builder);

    assertIssues().areNot(ruleKey("ForbiddenImportFilePatternRuleTest"));
  }
}
