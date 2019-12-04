package org.sonar.plugins.delphi.symbol.declaration;

import org.sonar.plugins.delphi.antlr.ast.node.QualifiedNameDeclarationNode;
import org.sonar.plugins.delphi.symbol.Qualifiable;
import org.sonar.plugins.delphi.symbol.QualifiedName;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;

public abstract class QualifiedDelphiNameDeclaration extends DelphiNameDeclaration
    implements Qualifiable {
  private final QualifiedName qualifiedName;

  protected QualifiedDelphiNameDeclaration(QualifiedNameDeclarationNode node) {
    super(node);
    qualifiedName = node.getQualifiedName();
  }

  protected QualifiedDelphiNameDeclaration(QualifiedNameDeclarationNode node, DelphiScope scope) {
    super(node, scope);
    qualifiedName = node.getQualifiedName();
  }

  @Override
  public QualifiedName getQualifiedName() {
    return qualifiedName;
  }
}
