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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.antlr.runtime.tree.CommonTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

public class FieldNameRule extends NameConventionRule {
  private static final String FIELD_PREFIX = "F";

  @Override
  public List<DelphiPMDNode> findNodes(DelphiPMDNode node) {
    if (node.getType() != DelphiLexer.TkClassField || isPublished()) {
      return Collections.emptyList();
    }

    return getFieldNames(node);
  }

  private List<DelphiPMDNode> getFieldNames(DelphiPMDNode node) {
    List<DelphiPMDNode> nodes = new ArrayList<>();
    CommonTree nameNode = (CommonTree) node.getFirstChildWithType(DelphiLexer.TkVariableIdents);

    if (nameNode != null) {
      CommonTree currentNode = nameNode;
      while ((currentNode = (CommonTree) currentNode.getChild(0)) != null) {
        nodes.add(new DelphiPMDNode(currentNode, node.getASTTree()));
      }
    }

    return nodes;
  }

  @Override
  protected boolean isViolation(DelphiPMDNode nameNode) {
    return !compliesWithPrefixNamingConvention(nameNode.getText(), FIELD_PREFIX);
  }
}
