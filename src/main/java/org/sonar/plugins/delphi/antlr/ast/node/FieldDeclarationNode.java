package org.sonar.plugins.delphi.antlr.ast.node;

import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class FieldDeclarationNode extends DelphiNode implements Visibility {
  public FieldDeclarationNode(Token token) {
    super(token);
  }

  public FieldDeclarationNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public VisibilityType getVisibility() {
    Node parent = jjtGetParent();
    if (parent instanceof FieldSectionNode) {
      return ((FieldSectionNode) parent).getVisibility();
    }
    return VisibilityType.PUBLIC;
  }

  public IdentifierListNode getIdentifierList() {
    return (IdentifierListNode) jjtGetChild(0);
  }
}
