package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class FieldSectionNode extends DelphiNode implements Visibility {
  public FieldSectionNode(Token token) {
    super(token);
  }

  public FieldSectionNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public VisibilityType getVisibility() {
    return ((VisibilitySectionNode) jjtGetParent()).getVisibility();
  }
}
