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
package au.com.integradev.delphi.pmd.rules;

import static au.com.integradev.delphi.pmd.DelphiPmdConstants.LIMIT;

import org.sonar.plugins.communitydelphi.api.ast.BlockDeclarationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.VarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.VarSectionNode;
import java.util.List;
import net.sourceforge.pmd.RuleContext;

public class TooManyVariablesRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    int count = countVariableDeclarations(method);
    int limit = getProperty(LIMIT);
    if (count > limit) {
      addViolationWithMessage(
          data,
          method.getMethodNameNode(),
          "Too many variables: {0} (max {1})",
          new Object[] {count, limit});
    }
    return super.visit(method, data);
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
