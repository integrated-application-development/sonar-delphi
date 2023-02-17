package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;

public interface ArrayAccessorNode extends DelphiNode {
  List<ExpressionNode> getExpressions();

  void setImplicitNameOccurrence(NameOccurrence implicitNameOccurrence);

  @Nullable
  NameOccurrence getImplicitNameOccurrence();
}
