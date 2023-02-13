package org.sonar.plugins.communitydelphi.api.ast;

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
