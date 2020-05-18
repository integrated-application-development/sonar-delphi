package org.sonar.plugins.delphi.antlr.ast.node;

import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class ConstSectionNode extends DelphiNode implements Visibility {
  public ConstSectionNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public VisibilityType getVisibility() {
    Node parent = jjtGetParent();
    if (parent instanceof VisibilitySectionNode) {
      return ((VisibilitySectionNode) parent).getVisibility();
    }
    return VisibilityType.PUBLIC;
  }
}
