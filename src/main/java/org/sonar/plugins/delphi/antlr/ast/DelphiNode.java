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
package org.sonar.plugins.delphi.antlr.ast;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

/**
 * Delphi Node used by ANTLR generated praser
 */
public class DelphiNode extends CommonTree {

  private ASTTree mainTree = null;

  /**
   * Default C-tor as in CommonTree
   * 
   * @param payload Provided token
   */
  DelphiNode(Token payload) {
    super(payload);
  }

  /**
   * C-tor with token and AST tree that has this node
   * 
   * @param payload Provided token
   * @param tree AST Tree
   */
  DelphiNode(Token payload, ASTTree tree) {
    super(payload);
    mainTree = tree;
  }

  /**
   * Gets child type, or -1 if child does not exist
   * 
   * @param index Child index
   * @return Child type, or -1 if child is non existant
   */
  public int getChildType(int index) {
    if (index > -1 && index < getChildCount()) {
      return getChild(index).getType();
    }
    return -1;
  }

  /**
   * Gets the AST Tree associated with this node
   * 
   * @return AST Tree
   */
  public ASTTree getASTTree() {
    return mainTree;
  }

}
