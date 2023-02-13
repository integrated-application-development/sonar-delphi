package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;

public interface ArgumentListNode extends DelphiNode {
  List<ExpressionNode> getArguments();

  boolean isEmpty();
}
