package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;
import javax.annotation.Nullable;

public interface ExceptBlockNode extends DelphiNode {
  @Nullable
  StatementListNode getStatementList();

  List<ExceptItemNode> getHandlers();

  @Nullable
  ElseBlockNode getElseBlock();

  boolean isBareExcept();

  boolean hasHandlers();
}
