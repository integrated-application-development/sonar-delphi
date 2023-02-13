package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;
import javax.annotation.Nullable;

public interface CaseStatementNode extends StatementNode {
  List<CaseItemStatementNode> getCaseItems();

  @Nullable
  ElseBlockNode getElseBlockNode();
}
