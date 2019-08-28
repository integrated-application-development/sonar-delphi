package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.symbol.Qualifiable;
import org.sonar.plugins.delphi.symbol.QualifiedName;

public final class QualifiedNameDeclarationNode extends NameDeclarationNode implements Qualifiable {

  private QualifiedName qualifiedName;

  public QualifiedNameDeclarationNode(Token token) {
    super(token);
  }

  public QualifiedNameDeclarationNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public QualifiedName getQualifiedName() {
    if (qualifiedName == null) {
      StringBuilder namePart = new StringBuilder();
      List<String> names = new ArrayList<>();

      for (int i = this.jjtGetNumChildren() - 1; i >= 0; --i) {
        Node child = this.jjtGetChild(i);
        namePart.insert(0, child.getImage());
        if (child instanceof IdentifierNode) {
          names.add(namePart.toString());
          namePart.setLength(0);
        }
      }

      Collections.reverse(names);

      qualifiedName = new QualifiedName(names);
    }
    return qualifiedName;
  }

  @Override
  public String getImage() {
    return fullyQualifiedName();
  }
}
