package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;

public interface RecordExpressionNode extends ExpressionNode {
  List<RecordExpressionItemNode> getItems();
}
