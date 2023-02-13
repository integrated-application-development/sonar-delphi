package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;

public interface ArrayConstructorNode extends ExpressionNode {
  List<ExpressionNode> getElements();
}
