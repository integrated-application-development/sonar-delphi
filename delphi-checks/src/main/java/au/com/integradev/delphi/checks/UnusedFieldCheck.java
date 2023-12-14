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

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.AttributeListNode;
import org.sonar.plugins.communitydelphi.api.ast.FieldDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceSectionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.SonarLintUnsupported;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@SonarLintUnsupported
@DeprecatedRuleKey(ruleKey = "UnusedFieldsRule", repositoryKey = "delph")
@Rule(key = "UnusedField")
public class UnusedFieldCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this unused field.";
  private static final boolean EXCLUDE_API_DEFAULT = false;

  @RuleProperty(
      key = "excludeApi",
      description = "Exclude fields declared in the interface section with public visibility.",
      defaultValue = EXCLUDE_API_DEFAULT + "")
  public boolean excludeApi = EXCLUDE_API_DEFAULT;

  @Override
  public DelphiCheckContext visit(FieldDeclarationNode field, DelphiCheckContext context) {
    if (isExcluded(field)) {
      return context;
    }

    field.getDeclarationList().getDeclarations().stream()
        .filter(node -> node.getUsages().isEmpty())
        .forEach(node -> reportIssue(context, node, MESSAGE));

    return context;
  }

  private boolean isExcluded(FieldDeclarationNode field) {
    if (field.isPublished()) {
      return true;
    }

    AttributeListNode attributeList = field.getAttributeList();
    if (attributeList != null && !attributeList.getAttributes().isEmpty()) {
      return true;
    }

    return excludeApi
        && field.isPublic()
        && field.getFirstParentOfType(InterfaceSectionNode.class) != null;
  }
}
