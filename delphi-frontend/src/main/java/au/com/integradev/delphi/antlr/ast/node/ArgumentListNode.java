package au.com.integradev.delphi.antlr.ast.node;

import java.util.List;

public interface ArgumentListNode extends DelphiNode {
  List<ExpressionNode> getArguments();

  boolean isEmpty();
}
