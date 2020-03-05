package org.sonar.plugins.delphi.antlr.ast.node;

import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public final class FieldDeclarationNode extends DelphiNode implements Typed, Visibility {
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

  public NameDeclarationListNode getDeclarationList() {
    return (NameDeclarationListNode) jjtGetChild(0);
  }

  public TypeNode getTypeNode() {
    return (TypeNode) jjtGetChild(1);
  }

  @Override
  @NotNull
  public Type getType() {
    return getTypeNode().getType();
  }
}
