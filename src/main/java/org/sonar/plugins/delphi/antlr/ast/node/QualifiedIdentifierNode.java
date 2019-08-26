package org.sonar.plugins.delphi.antlr.ast.node;

import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class QualifiedIdentifierNode extends DelphiNode {
  private String simpleName;
  private String qualifiedName;

  public QualifiedIdentifierNode(Token token) {
    super(token);
  }

  public QualifiedIdentifierNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  private void buildNames() {
    StringBuilder nameBuilder = new StringBuilder();

    for (int i = this.jjtGetNumChildren() - 1; i >= 0; --i) {
      Node child = this.jjtGetChild(i);
      nameBuilder.insert(0, child.getImage());
      if ((child instanceof IdentifierNode)) {
        if (simpleName == null) {
          simpleName = nameBuilder.toString();
        }
        if (i > 0) {
          nameBuilder.insert(0, '.');
        }
      }
    }

    qualifiedName = nameBuilder.toString();
  }

  public String getSimpleName() {
    if (simpleName == null) {
      buildNames();
    }

    return simpleName;
  }

  public String getQualifiedName() {
    if (qualifiedName == null) {
      buildNames();
    }

    return qualifiedName;
  }

  @Override
  public String getImage() {
    return getQualifiedName();
  }
}
