/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colombo
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
package org.sonar.plugins.delphi.pmd.rules;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class MethodNameRule extends NameConventionRule {

  @Override
  public List<DelphiPMDNode> findNodes(DelphiPMDNode node) {
    if (node.getType() != DelphiLexer.TkNewType || (!isInterface(node) && isPublished())) {
      return Collections.emptyList();
    }

    List<Tree> nameParentNodes = node.findAllChildren(DelphiLexer.TkFunctionName);

    return nameParentNodes.stream()
        .map(parent -> new DelphiPMDNode((CommonTree) parent.getChild(0), node.getASTTree()))
        .collect(Collectors.toList());
  }

  @Override
  protected boolean isViolation(DelphiPMDNode method) {
    String name = method.getText();
    return Character.isLowerCase(name.charAt(0));
  }

  private boolean isInterface(DelphiPMDNode typeNode) {
    Tree typeDeclNode = typeNode.getFirstChildWithType(DelphiLexer.TkNewTypeDecl);
    int type = typeDeclNode.getChild(0).getType();

    return type == DelphiLexer.TkInterface;
  }
}
