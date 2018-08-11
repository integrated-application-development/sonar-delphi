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
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * This rule will find any public fields in class declaration(s) and raise violations on them.
 */
public class PublicFieldsRule extends DelphiRule {

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx){

      if(node.getType() == DelphiLexer.TkClass){ // Wherever there is a class definition
      Tree classNode = node;
      boolean inPublic = false;
      for(int i = 0; i  < classNode.getChildCount(); i++ ){ // visits all its children
        Tree child = classNode.getChild(i);
        //do nothing until the public section.
        if(inPublic){
            if(child.getType() != DelphiLexer.TkClassField && child.getType() != DelphiLexer.PROPERTY
                    && child.getType() != DelphiLexer.PROCEDURE && child.getType() != DelphiLexer.CONSTRUCTOR){ // Check if still in public before continuing
                inPublic = false;
                break;

            }
          if(child.getType() == DelphiLexer.TkClassField){// raise violations on any fields
              addViolation(ctx, (DelphiPMDNode) child);
          }
        }
        else {
            if (child.getType() == DelphiLexer.PUBLIC){//Are we there yet?
                inPublic = true;
            }
        }
      }

    }

  }
}
