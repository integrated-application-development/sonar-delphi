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

import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Rule that count's the number of lines between to statement pairs. Produces a violation if the number of lines exceeds limit.
 */
public class BlockCounterRule extends CountRule {

  protected boolean isCounting;
  protected int lastLine;
  protected String start;
  protected String end;
  protected DelphiPMDNode firstNode;
  protected int startIndex;

  @Override
  protected void init() {
    super.init();
    start = getStringProperty("start");
    end = getStringProperty("end");
    lastLine = 0;
    startIndex = 0;
    isCounting = false;
    firstNode = null;
  }

  /**
   * Should we count the current node?
   * 
   * @param node
   *          Node to check
   */
  @Override
  protected boolean shouldCount(DelphiPMDNode node) {
    if ( !isCounting && isStartNode(node)) { // begin counting
      isCounting = true;
      count = 0;
      lastLine = node.getLine();
      firstNode = node;
      startIndex = 1;
    } else if (isCounting) { // already counting
      if (isEndNode(node) && --startIndex == 0) { // stop counting and check for violation
        isCounting = false;
        lastLine = 0;
      } else if (isStartNode(node)) { // if another start node is encountered
        ++startIndex;
      } else if (accept(node)) {
        lastLine = node.getLine();
        return true; // increment counter
      }
    }

    return false;
  }

  /**
   * Overload, so we could pass the firstNode
   */
  @Override
  protected void addViolation(Object data, DelphiPMDNode node) {
    super.addViolation(data, firstNode);
  }

  /**
   * Is current node the start node
   * 
   * @param node
   *          Node
   * @return True if so, false otherwise
   */
  protected boolean isStartNode(DelphiPMDNode node) {
    return node.getText().equals(start);
  }

  /**
   * Is current node the end node
   * 
   * @param node
   *          Node
   * @return True if so, false otherwise
   */
  protected boolean isEndNode(DelphiPMDNode node) {
    return node.getText().equals(end);
  }

  /**
   * Should we accept the current node, and count it?
   * 
   * @param node
   *          Node to count
   * @return True if so, false otherwise
   */
  protected boolean accept(DelphiPMDNode node) {
    return node.getLine() > lastLine;
  }

}
