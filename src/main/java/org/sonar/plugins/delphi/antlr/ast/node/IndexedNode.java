package org.sonar.plugins.delphi.antlr.ast.node;

import net.sourceforge.pmd.lang.ast.Node;

public interface IndexedNode extends Node {

  /**
   * Returns the node's unique token index
   *
   * @return Token index
   */
  int getTokenIndex();
}
