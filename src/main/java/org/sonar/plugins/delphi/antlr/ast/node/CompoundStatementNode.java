package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import java.util.stream.Stream;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class CompoundStatementNode extends StatementNode {
  public CompoundStatementNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public StatementListNode getStatementList() {
    return ((StatementListNode) jjtGetChild(0));
  }

  public List<StatementNode> getStatements() {
    return getStatementList().getStatements();
  }

  public boolean isEmpty() {
    return getStatementList().isEmpty();
  }

  public Stream<StatementNode> statementStream() {
    return getStatementList().statementStream();
  }
}
