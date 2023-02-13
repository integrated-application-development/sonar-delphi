package org.sonar.plugins.communitydelphi.api.ast;

public interface ForStatementNode extends StatementNode {
  ForLoopVarNode getVariable();

  StatementNode getStatement();
}
