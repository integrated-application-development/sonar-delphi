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

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ConstDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.ConstStatementNode;
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

  @Override
  public DelphiCheckContext visit(
      ConstDeclarationNode constDeclaration, DelphiCheckContext context) {
    NameDeclarationNode name = constDeclaration.getNameDeclarationNode();
    if (name.getUsages().isEmpty()) {
      reportIssue(context, name, MESSAGE);
    }
    return context;
  }

  @Override
  public DelphiCheckContext visit(ConstStatementNode constStatement, DelphiCheckContext context) {
    NameDeclarationNode name = constStatement.getNameDeclarationNode();
    if (name.getUsages().isEmpty()) {
      reportIssue(context, name, MESSAGE);
    }
    return context;
  }
}
