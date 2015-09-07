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
package org.sonar.plugins.delphi.antlr.analyzer;

import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;

/**
 * Code analyzed class provides us with information about code being currently
 * analysed.
 */
public class CodeTree {

  private CodeNode<ASTTree> rootNode;
  private CodeNode<Tree> currentNode;

  /**
   * ctor
   * 
   * @param root root element
   * @param current first element we will parse
   */
  public CodeTree(CodeNode<ASTTree> root, CodeNode<Tree> current) {
    this.rootNode = root;
    this.currentNode = current;
  }

  /**
   * @return current node
   */
  public CodeNode<Tree> getCurrentCodeNode() {
    return currentNode;
  }

  /**
   * @return root node
   */
  public CodeNode<ASTTree> getRootCodeNode() {
    return rootNode;
  }

  /**
   * sets the current node
   * 
   * @param n node
   */
  public void setCurrentNode(CodeNode<Tree> n) {
    currentNode = n;
  }

}
