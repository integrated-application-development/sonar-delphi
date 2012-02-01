/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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

import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * "Try" should always be preceeded with "Begin" after "Then"
 */
public class ThenTryRule extends DelphiRule {

  @Override
  public Object visit(DelphiPMDNode node, Object data) {
    if (node.getType() != DelphiLexer.THEN) {
      return data; // if not "THEN" node
    }
    DelphiNode parent = (DelphiNode) node.getParent(); // get parent
    int nextNode = parent.getChildType(node.getChildIndex() + 1); // get next node ident
    if (nextNode == DelphiLexer.TRY) {
      addViolation(data, node); // if next node is "TRY"
    }
    return data;
  }
}
