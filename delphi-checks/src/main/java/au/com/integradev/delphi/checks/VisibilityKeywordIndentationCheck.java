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
package au.com.integradev.delphi.checks;

import au.com.integradev.delphi.utils.IndentationUtils;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.VisibilityNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "VisibilityKeywordIndentationRule", repositoryKey = "delph")
@Rule(key = "VisibilityKeywordIndentation")
public class VisibilityKeywordIndentationCheck extends DelphiCheck {
  private static final String MESSAGE =
      "Indent this visibility specifier to the indentation level of the containing type.";

  private static String getExpectedIndentation(DelphiNode node) {
    var visibilityNode = (VisibilityNode) node;
    // Class/Record/etc. -> VisibilitySection -> Visibility
    var parent = visibilityNode.getParent().getParent();
    return IndentationUtils.getLineIndentation(parent);
  }

  @Override
  public DelphiCheckContext visit(VisibilityNode visibilityNode, DelphiCheckContext context) {
    if (!IndentationUtils.getLineIndentation(visibilityNode)
        .equals(getExpectedIndentation(visibilityNode))) {
      reportIssue(context, visibilityNode, MESSAGE);
    }
    return super.visit(visibilityNode, context);
  }
}
