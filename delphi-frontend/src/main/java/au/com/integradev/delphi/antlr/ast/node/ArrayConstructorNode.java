package au.com.integradev.delphi.antlr.ast.node;

import java.util.List;

public interface ArrayConstructorNode extends ExpressionNode {
  List<ExpressionNode> getElements();
}
