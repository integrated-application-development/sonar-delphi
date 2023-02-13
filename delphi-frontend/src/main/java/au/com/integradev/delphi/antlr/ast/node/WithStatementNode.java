package au.com.integradev.delphi.antlr.ast.node;

import java.util.List;
import javax.annotation.Nullable;

public interface WithStatementNode extends StatementNode {
  List<ExpressionNode> getTargets();

  @Nullable
  StatementNode getStatement();
}
