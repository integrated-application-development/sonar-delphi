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

import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

/**
 * This rule looks for the interface name and if it doesn't start with "I" it raises a violation.
 */
public class InterfaceNameRule extends NameConventionRule {
  private static final String INTERFACE_PREFIX = "I";

  @Override
  public DelphiNode findNode(DelphiNode node) {
    if (node.getType() != DelphiLexer.TkNewTypeName
        || node.nextNode().getChildType(0) != DelphiLexer.TkInterface) {
      return null;
    }

    return (DelphiNode) node.getChild(0);
  }

  @Override
  protected boolean isViolation(DelphiNode nameNode) {
    return !compliesWithPrefixNamingConvention(nameNode.getText(), INTERFACE_PREFIX);
  }
}
