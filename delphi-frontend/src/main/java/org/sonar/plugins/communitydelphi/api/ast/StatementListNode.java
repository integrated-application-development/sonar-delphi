package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;
import java.util.stream.Stream;

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
 *   <li>{@code try {statementList} except {statementList} end}
 *   <li>{@code repeat {statementList} until {expression}}
 * </ul>
 */
public interface StatementListNode extends DelphiNode {

  boolean isEmpty();

  List<StatementNode> getStatements();

  List<StatementNode> getDescendantStatements();

  Stream<StatementNode> statementStream();

  Stream<StatementNode> descendantStatementStream();
}
