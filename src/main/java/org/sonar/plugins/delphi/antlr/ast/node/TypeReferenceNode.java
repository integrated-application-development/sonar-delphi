package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.symbol.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.Qualifiable;
import org.sonar.plugins.delphi.symbol.QualifiedName;
import org.sonar.plugins.delphi.type.DelphiUnresolvedType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public final class TypeReferenceNode extends TypeNode implements Qualifiable {
  public TypeReferenceNode(Token token) {
    super(token);
  }

  public TypeReferenceNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  private NameReferenceNode getNameNode() {
    return ((NameReferenceNode) jjtGetChild(0));
  }

  @Override
  @NotNull
  public Type createType() {
    DelphiNameDeclaration declaration = getNameNode().getLastName().getNameDeclaration();
    if (declaration instanceof Typed) {
      return ((Typed) declaration).getType();
    }
    return DelphiUnresolvedType.referenceTo(getNameNode());
  }

  @Override
  public QualifiedName getQualifiedName() {
    return getNameNode().getQualifiedName();
  }
}
