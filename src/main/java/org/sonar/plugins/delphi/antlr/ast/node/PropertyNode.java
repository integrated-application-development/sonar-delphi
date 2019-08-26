package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class PropertyNode extends DelphiNode implements Visibility {
  private VisibilityType visibility;

  public PropertyNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public VisibilityType getVisibility() {
    if (visibility == null) {
      visibility = ((VisibilitySectionNode) jjtGetParent()).getVisibility();
    }
    return visibility;
  }
}
