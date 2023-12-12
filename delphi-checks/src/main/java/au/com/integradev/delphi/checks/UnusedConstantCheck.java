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
import org.sonar.plugins.communitydelphi.api.ast.ConstDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.ConstStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.SonarLintUnsupported;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@SonarLintUnsupported
@DeprecatedRuleKey(ruleKey = "UnusedConstantsRule", repositoryKey = "delph")
@Rule(key = "UnusedConstant")
public class UnusedConstantCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this unused constant.";

  @RuleProperty(
      key = "excludeApi",
      description = "Exclude constants declared in the interface section with public visibility.")
  public boolean excludeApi = false;

  @Override
  public DelphiCheckContext visit(ConstDeclarationNode declaration, DelphiCheckContext context) {
    if (excludeApi
        && (declaration.isPublished() || declaration.isPublic())
        && declaration.getFirstParentOfType(InterfaceSectionNode.class) != null) {
      return context;
    }

    NameDeclarationNode name = declaration.getNameDeclarationNode();
    if (name.getUsages().isEmpty()) {
      reportIssue(context, name, MESSAGE);
    }
    return context;
  }

  @Override
  public DelphiCheckContext visit(ConstStatementNode statement, DelphiCheckContext context) {
    NameDeclarationNode name = statement.getNameDeclarationNode();
    if (name.getUsages().isEmpty()) {
      reportIssue(context, name, MESSAGE);
    }
    return context;
  }
}
