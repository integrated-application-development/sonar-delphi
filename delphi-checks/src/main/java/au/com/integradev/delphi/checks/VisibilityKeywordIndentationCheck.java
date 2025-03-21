/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.VisibilityNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "VisibilityKeywordIndentationRule", repositoryKey = "delph")
@Rule(key = "VisibilityKeywordIndentation")
public class VisibilityKeywordIndentationCheck extends DelphiCheck {
  private static final String MESSAGE =
      "Indent this visibility specifier to the indentation level of the containing type.";

  @Override
  public DelphiCheckContext visit(VisibilityNode visibilityNode, DelphiCheckContext context) {
    var declaration = visibilityNode.getNthParent(3);

    if (declaration instanceof TypeDeclarationNode) {
      NameDeclarationNode typeName = ((TypeDeclarationNode) declaration).getTypeNameNode();

      String actual = IndentationUtils.getLineIndentation(visibilityNode);
      String expected = IndentationUtils.getLineIndentation(typeName);

      if (!actual.equals(expected)) {
        reportIssue(context, visibilityNode, MESSAGE);
      }
    }

    return super.visit(visibilityNode, context);
  }
}
