package org.sonar.plugins.delphi.antlr.ast.node;

import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class VisibilitySectionNode extends DelphiNode implements Visibility {
  private VisibilityType visibilityType;

  public VisibilitySectionNode(Token token) {
    super(token);
  }

  public VisibilitySectionNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public VisibilityType getVisibility() {
    if (visibilityType == null) {
      Node child = jjtGetChild(0);
      if (child instanceof VisibilityNode) {
        visibilityType = ((VisibilityNode) child).getVisibility();
      } else {
        visibilityType = VisibilityType.IMPLICIT_PUBLISHED;
      }
    }
    return visibilityType;
  }
}
