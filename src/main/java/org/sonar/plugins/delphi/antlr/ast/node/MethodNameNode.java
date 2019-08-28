package org.sonar.plugins.delphi.antlr.ast.node;

import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.symbol.Qualifiable;
import org.sonar.plugins.delphi.symbol.QualifiedName;

public final class MethodNameNode extends NameDeclarationNode implements Qualifiable {
  public MethodNameNode(Token token) {
    super(token);
  }

  public MethodNameNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public NameReferenceNode getNameReferenceNode() {
    Node child = jjtGetChild(0);
    return (child instanceof NameReferenceNode) ? (NameReferenceNode) child : null;
  }

  public QualifiedNameDeclarationNode getNameDeclarationNode() {
    Node child = jjtGetChild(0);
    return (child instanceof NameDeclarationNode) ? (QualifiedNameDeclarationNode) child : null;
  }

  @Override
  public QualifiedName getQualifiedName() {
    return ((Qualifiable) jjtGetChild(0)).getQualifiedName();
  }
}
