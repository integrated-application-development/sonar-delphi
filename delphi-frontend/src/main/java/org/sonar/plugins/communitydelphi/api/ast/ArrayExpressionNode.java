package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;

public interface ArrayExpressionNode extends ExpressionNode {
  List<ExpressionNode> getElements();
}
