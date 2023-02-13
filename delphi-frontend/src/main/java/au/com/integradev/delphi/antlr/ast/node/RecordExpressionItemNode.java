package au.com.integradev.delphi.antlr.ast.node;

public interface RecordExpressionItemNode extends DelphiNode {
  IdentifierNode getIdentifier();

  ExpressionNode getExpression();
}
