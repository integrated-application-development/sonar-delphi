package au.com.integradev.delphi.antlr.ast.node;

import java.util.List;
import java.util.stream.Stream;

public interface CompoundStatementNode extends StatementNode {
  StatementListNode getStatementList();

  List<StatementNode> getStatements();

  List<StatementNode> getDescendentStatements();

  Stream<StatementNode> statementStream();

  Stream<StatementNode> descendantStatementStream();

  boolean isEmpty();
}
