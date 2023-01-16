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
package org.sonar.plugins.communitydelphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.communitydelphi.antlr.ast.DelphiAST;
import org.sonar.plugins.communitydelphi.antlr.ast.node.ConstDeclarationNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.MethodNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.StatementNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.communitydelphi.pmd.FilePosition;

public class EmptyUnitRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(DelphiAST ast, RuleContext data) {
    if (!ast.isPackage() && !hasMeaningfulCode(ast)) {
      newViolation(data).atPosition(FilePosition.atFileLevel()).save();
    }
    return data;
  }

  private static boolean hasMeaningfulCode(DelphiNode node) {
    return node.hasDescendantOfAnyType(
        MethodNode.class,
        StatementNode.class,
        VarDeclarationNode.class,
        ConstDeclarationNode.class,
        TypeDeclarationNode.class);
  }
}
