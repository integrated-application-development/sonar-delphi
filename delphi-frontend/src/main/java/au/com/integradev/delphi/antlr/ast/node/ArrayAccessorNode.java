package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.symbol.NameOccurrence;
import java.util.List;
import javax.annotation.Nullable;

public interface ArrayAccessorNode extends DelphiNode {
  List<ExpressionNode> getExpressions();

  void setImplicitNameOccurrence(NameOccurrence implicitNameOccurrence);

  @Nullable
  NameOccurrence getImplicitNameOccurrence();
}
