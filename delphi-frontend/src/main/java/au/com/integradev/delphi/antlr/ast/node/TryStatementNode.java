package au.com.integradev.delphi.antlr.ast.node;

public interface TryStatementNode extends StatementNode {
  StatementListNode getStatementList();

  boolean hasExceptBlock();

  boolean hasFinallyBlock();

  ExceptBlockNode getExpectBlock();

  FinallyBlockNode getFinallyBlock();
}
