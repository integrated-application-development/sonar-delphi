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

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.BlockDeclarationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.VarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.VarSectionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "TooManyVariablesRule", repositoryKey = "delph")
@Rule(key = "TooManyVariables")
public class TooManyVariablesCheck extends DelphiCheck {
  private static final int DEFAULT_MAXIMUM = 10;

  @RuleProperty(
      key = "max",
      description = "Maximum authorized number of variables",
      defaultValue = "" + DEFAULT_MAXIMUM)
  public int max = DEFAULT_MAXIMUM;

  @Override
  public DelphiCheckContext visit(MethodImplementationNode method, DelphiCheckContext context) {
    int count = countVariableDeclarations(method);
    if (count > max) {
      reportIssue(
          context,
          method.getMethodNameNode(),
          String.format(
              "Method has %d variables, which is greater than %d authorized.", count, max));
    }
    return super.visit(method, context);
  }

  private static int countVariableDeclarations(MethodImplementationNode method) {
    int count = 0;
    BlockDeclarationSectionNode declSection = method.getDeclarationSection();
    if (declSection != null) {
      List<VarSectionNode> varSections = declSection.findChildrenOfType(VarSectionNode.class);
      for (VarSectionNode varSection : varSections) {
        for (VarDeclarationNode varDeclaration : varSection.getDeclarations()) {
          count += varDeclaration.getNameDeclarationList().getDeclarations().size();
        }
      }
    }
    return count;
  }
}
