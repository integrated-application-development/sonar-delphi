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
import org.sonar.plugins.communitydelphi.api.ast.ConstDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.VarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "EmptyUnitRule", repositoryKey = "delph")
@Rule(key = "EmptyFile")
public class EmptyFileCheck extends DelphiCheck {
  private static final String MESSAGE = "This file has 0 lines of code.";

  @Override
  public DelphiCheckContext visit(DelphiAst ast, DelphiCheckContext context) {
    if (!ast.isPackage() && !hasExecutableCode(ast) && !hasDeclarations(ast)) {
      context.newIssue().withMessage(MESSAGE).report();
    }
    return context;
  }

  private static boolean hasExecutableCode(DelphiNode node) {
    return node.hasDescendantOfType(StatementNode.class);
  }

  private static boolean hasDeclarations(DelphiNode node) {
    return node.hasDescendantOfType(RoutineNode.class)
        || node.hasDescendantOfType(VarDeclarationNode.class)
        || node.hasDescendantOfType(ConstDeclarationNode.class)
        || node.hasDescendantOfType(TypeDeclarationNode.class);
  }
}
