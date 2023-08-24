/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package org.sonar.plugins.communitydelphi.api.check;

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

public abstract class DelphiCheck implements DelphiParserVisitor<DelphiCheckContext> {
  /**
   * A rule parameter that allows the user to override the rule scope specified in the metadata
   * JSON.
   *
   * <p>This parameter is only surfaced for template rules.
   */
  @RuleProperty(
      key = "scope",
      description =
          "The type of code this rule should apply to. Options are: 'ALL', 'MAIN', 'TEST'.")
  public String customRuleScopeOverride = "";

  public void start(DelphiCheckContext context) {
    // do nothing
  }

  public void end(DelphiCheckContext context) {
    // do nothing
  }

  protected void reportIssue(DelphiCheckContext context, DelphiNode node, String message) {
    context.newIssue().onNode(node).withMessage(message).report();
  }
}
