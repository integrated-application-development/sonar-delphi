package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import java.util.List;
import javax.annotation.Nullable;

public interface ArrayAccessorNode extends DelphiNode {
  List<ExpressionNode> getExpressions();

  void setImplicitNameOccurrence(NameOccurrence implicitNameOccurrence);

  @Nullable
  NameOccurrence getImplicitNameOccurrence();
}
