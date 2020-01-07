package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import java.util.stream.Stream;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

/**
 * Statement Lists are effectively implicit compound statements that Delphi uses in a few places.
 * The StatementListNode is not itself a statement.
 *
 * <p>In terms of file position, Statement Lists are entirely abstract, meaning they have an
 * imaginary token at their root and can also be empty. In light of this, StatementListNodes will
 * always return their parents file position.
 *
 * <p>Examples:
 *
 * <ul>
 *   <li><code>try {statementList} except {statementList} end</code>
 *   <li><code>repeat {statementList} until {expression}</code>
 * </ul>
 */
public final class StatementListNode extends DelphiNode {
  private List<StatementNode> statements;
  private List<StatementNode> descendantStatements;

  public StatementListNode(Token token) {
    super(token);
  }

  public StatementListNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public boolean isEmpty() {
    return this.jjtGetNumChildren() == 0;
  }

  public List<StatementNode> getStatements() {
    if (statements == null) {
      statements = findChildrenOfType(StatementNode.class);
    }
    return statements;
  }

  public List<StatementNode> getDescendantStatements() {
    if (descendantStatements == null) {
      descendantStatements = findDescendantsOfType(StatementNode.class);
    }
    return descendantStatements;
  }

  public Stream<StatementNode> statementStream() {
    return getStatements().stream();
  }

  public Stream<StatementNode> descendantStatementStream() {
    return getDescendantStatements().stream();
  }

  @Override
  public int getBeginLine() {
    return jjtGetParent().getBeginLine();
  }

  @Override
  public int getBeginColumn() {
    return jjtGetParent().getBeginColumn();
  }

  @Override
  public int getEndLine() {
    return jjtGetParent().getEndLine();
  }

  @Override
  public int getEndColumn() {
    return jjtGetParent().getEndColumn();
  }
}
