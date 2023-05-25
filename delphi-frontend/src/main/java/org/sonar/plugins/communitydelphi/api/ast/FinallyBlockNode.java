package org.sonar.plugins.communitydelphi.api.ast;

public interface FinallyBlockNode extends DelphiNode {
  StatementListNode getStatementList();
}
