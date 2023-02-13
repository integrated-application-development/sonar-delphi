package org.sonar.plugins.communitydelphi.api.ast;

public interface TryStatementNode extends StatementNode {
  StatementListNode getStatementList();

  boolean hasExceptBlock();

  boolean hasFinallyBlock();

  ExceptBlockNode getExpectBlock();

  FinallyBlockNode getFinallyBlock();
}
