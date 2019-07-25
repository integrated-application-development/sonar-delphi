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

import org.antlr.runtime.tree.CommonTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

/**
 * This rule looks for the interface name and if it doesn't start with "I" it raises a violation.
 */
public class InterfaceNameRule extends NameConventionRule {
  private static final String INTERFACE_PREFIX = "I";

  @Override
  public DelphiPMDNode findNode(DelphiPMDNode node) {
    if (node.getType() != DelphiLexer.TkNewTypeName
        || node.nextNode().getType() != DelphiLexer.TkInterface) {
      return null;
    }

    return new DelphiPMDNode((CommonTree) node.getChild(0), node.getASTTree());
  }

  @Override
  protected boolean isViolation(DelphiPMDNode nameNode) {
    return !compliesWithPrefixNamingConvention(nameNode.getText(), INTERFACE_PREFIX);
  }
}
