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
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

public class FieldNameRule extends NameConventionRule {
  private static final String FIELD_PREFIX = "F";

  @Override
  public List<DelphiNode> findNodes(DelphiNode node) {
    if (node.getType() != DelphiLexer.TkClassField || isPublished()) {
      return Collections.emptyList();
    }

    return getFieldNames(node);
  }

  private List<DelphiNode> getFieldNames(DelphiNode node) {
    List<DelphiNode> nodes = new ArrayList<>();
    CommonTree nameNode = (CommonTree) node.getFirstChildWithType(DelphiLexer.TkVariableIdents);

    if (nameNode != null) {
      CommonTree currentNode = nameNode;
      while ((currentNode = (CommonTree) currentNode.getChild(0)) != null) {
        nodes.add((DelphiNode) currentNode);
      }
    }

    return nodes;
  }

  @Override
  protected boolean isViolation(DelphiNode nameNode) {
    return !compliesWithPrefixNamingConvention(nameNode.getText(), FIELD_PREFIX);
  }
}
