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
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

/** This rule will find any public fields in class declaration(s) and raise violations on them. */
public class PublicFieldsRule extends DelphiRule {

  /**
   * This rule searches for any fields declared under a 'public' block which are also fields. These
   * should be avoided, so a violation will be raised if any of these types are declared under a
   * 'public' block.
   *
   * @param node the current node
   * @param ctx the ruleContext to store the violations
   */
  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {

    // Wherever there is a class definition
    if (node.getType() == DelphiLexer.TkClass) {
      boolean inPublic = false;

      // visits all its children
      for (int i = 0; i < node.getChildCount(); i++) {
        Tree child = node.getChild(i);
        // Do nothing until the public section.
        if (!inPublic) {
          inPublic = (child.getType() == DelphiLexer.PUBLIC);
          continue;
        }

        // Check if still in public before continuing
        if (child.getType() != DelphiLexer.TkClassField
            && child.getType() != DelphiLexer.PROPERTY
            && child.getType() != DelphiLexer.PROCEDURE
            && child.getType() != DelphiLexer.CONSTRUCTOR) {
          return;
        }

        // raise violations on any fields
        if (child.getType() == DelphiLexer.TkClassField) {
          addViolation(ctx, (DelphiPMDNode) child);
        }
      }
    }
  }
}
