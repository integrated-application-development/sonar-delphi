package au.com.integradev.delphi.antlr.ast.node;

import java.util.List;
import javax.annotation.Nullable;

public interface ExceptBlockNode extends DelphiNode {
  @Nullable
  StatementListNode getStatementList();

  List<ExceptItemNode> getHandlers();

  @Nullable
  ElseBlockNode getElseBlock();

  boolean isBareExcept();

  boolean hasHandlers();
}
