package au.com.integradev.delphi.antlr.ast.node;

public interface ForStatementNode extends StatementNode {
  ForLoopVarNode getVariable();

  StatementNode getStatement();
}
