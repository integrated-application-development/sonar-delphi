package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.symbol.scope.DelphiScope;

public interface Node {
  int jjtGetId();

  /**
   * Returns the node's unique token index
   *
   * @return Token index
   */
  int getTokenIndex();

  String getImage();

  int getBeginLine();

  int getBeginColumn();

  int getEndLine();

  int getEndColumn();

  DelphiScope getScope();

  /**
   * Returns the name of the unit where this node is located
   *
   * @return the name of the unit where this node is located
   */
  String getUnitName();
}
