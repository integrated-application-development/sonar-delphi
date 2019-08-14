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
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

public class UnitNameRule extends NameConventionRule {
  private static final String[] PREFIXES = {""};

  @Override
  public List<DelphiNode> findNodes(DelphiNode node) {
    if (node.getType() == DelphiLexer.UNIT) {
      List<?> children = node.getChildren();

      return children.stream()
          .filter(child -> ((CommonTree) child).getType() != DelphiLexer.DOT)
          .map(child -> (DelphiNode) child)
          .collect(Collectors.toList());
    }

    return Collections.emptyList();
  }

  @Override
  protected boolean isViolation(DelphiNode nameNode) {
    return !compliesWithPrefixNamingConvention(nameNode.getText(), PREFIXES);
  }
}
