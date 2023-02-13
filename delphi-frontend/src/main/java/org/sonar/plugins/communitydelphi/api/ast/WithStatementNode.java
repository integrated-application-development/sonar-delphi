package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;
import javax.annotation.Nullable;

public interface WithStatementNode extends StatementNode {
  List<ExpressionNode> getTargets();

  @Nullable
  StatementNode getStatement();
}
