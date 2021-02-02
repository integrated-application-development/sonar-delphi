package org.sonar.plugins.delphi.antlr.ast.node;

import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.CodePages;
import org.sonar.plugins.delphi.type.Type;

public final class AnsiStringTypeNode extends TypeNode {
  public AnsiStringTypeNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  private int getCodePage() {
    Node node = jjtGetChild(0);
    if (node instanceof ExpressionNode) {
      LiteralNode codePage = ((ExpressionNode) node).extractLiteral();
      if (codePage != null) {
        return codePage.getValueAsInt();
      }
    }
    return CodePages.CP_ACP;
  }

  @Override
  @NotNull
  public Type createType() {
    return getTypeFactory().ansiString(getCodePage());
  }
}
