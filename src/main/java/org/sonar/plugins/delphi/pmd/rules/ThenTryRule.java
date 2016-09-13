// $ANTLR 3.5.2 org\\sonar\\plugins\\delphi\\antlr\\Delphi.g 2016-07-19 17:58:30

/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * "Try" should always be preceeded with "Begin" after "Then"
 * 
 */
public class ThenTryRule extends DelphiRule {

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    if (node.getType() != DelphiLexer.THEN) {
      return;
    }

    DelphiNode parent = (DelphiNode) node.getParent();

    // get next node ident
    int nextNode = parent.getChildType(node.getChildIndex() + 1);

    if (nextNode == DelphiLexer.TRY) {
      addViolation(ctx, node);
    }
  }
}
