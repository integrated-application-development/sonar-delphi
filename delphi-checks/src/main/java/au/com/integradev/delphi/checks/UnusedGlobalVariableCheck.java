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

import static au.com.integradev.delphi.utils.VariableUtils.isGeneratedFormVariable;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.VarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.SonarLintUnsupported;
import org.sonar.plugins.communitydelphi.api.symbol.scope.FileScope;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@SonarLintUnsupported
@DeprecatedRuleKey(ruleKey = "UnusedGlobalVariablesRule", repositoryKey = "delph")
@Rule(key = "UnusedGlobalVariable")
public class UnusedGlobalVariableCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this unused global variable.";
  private static final boolean EXCLUDE_API_DEFAULT = false;

  @RuleProperty(
      key = "excludeApi",
      description = "Exclude global variables declared in the interface section.",
      defaultValue = EXCLUDE_API_DEFAULT + "")
  public boolean excludeApi = EXCLUDE_API_DEFAULT;

  @Override
  public DelphiCheckContext visit(VarDeclarationNode varDeclaration, DelphiCheckContext context) {
    if (isExcluded(varDeclaration)) {
      return context;
    }

    varDeclaration.getNameDeclarationList().getDeclarations().stream()
        .filter(node -> node.getUsages().isEmpty())
        .forEach(node -> reportIssue(context, node, MESSAGE));

    return context;
  }

  private boolean isExcluded(VarDeclarationNode varDeclaration) {
    if (!(varDeclaration.getScope() instanceof FileScope)) {
      return true;
    }

    if (excludeApi && varDeclaration.getFirstParentOfType(InterfaceSectionNode.class) != null) {
      return true;
    }

    return isGeneratedFormVariable(varDeclaration);
  }
}
