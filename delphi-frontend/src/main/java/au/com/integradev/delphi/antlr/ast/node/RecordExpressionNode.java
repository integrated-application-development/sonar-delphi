package au.com.integradev.delphi.antlr.ast.node;

import java.util.List;

public interface RecordExpressionNode extends ExpressionNode {
  List<RecordExpressionItemNode> getItems();
}
