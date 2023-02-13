package au.com.integradev.delphi.antlr.ast.node;

import java.util.List;
import javax.annotation.Nullable;

public interface CaseStatementNode extends StatementNode {
  List<CaseItemStatementNode> getCaseItems();

  @Nullable
  ElseBlockNode getElseBlockNode();
}
