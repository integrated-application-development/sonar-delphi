package org.sonar.plugins.delphi.antlr.ast.node;

import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.symbol.declaration.UnitImportNameDeclaration;

public final class UnitImportNode extends DelphiNode {
  public UnitImportNode(Token token) {
    super(token);
  }

  public UnitImportNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public QualifiedNameDeclarationNode getNameNode() {
    return (QualifiedNameDeclarationNode) jjtGetChild(0);
  }

  public boolean isResolvedImport() {
    NameDeclaration declaration = getNameNode().getNameDeclaration();
    return declaration != null
        && ((UnitImportNameDeclaration) declaration).getOriginalDeclaration() != null;
  }

  public UnitImportNameDeclaration getImportNameDeclaration() {
    return (UnitImportNameDeclaration) getNameNode().getNameDeclaration();
  }
}
