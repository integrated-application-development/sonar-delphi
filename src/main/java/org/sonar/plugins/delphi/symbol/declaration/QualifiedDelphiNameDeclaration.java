package org.sonar.plugins.delphi.symbol.declaration;

import java.util.Objects;
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

  @Override
  public boolean equals(Object o) {
    return super.equals(o)
        && getQualifiedNameParts()
            .equals(((QualifiedDelphiNameDeclaration) o).getQualifiedNameParts());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), qualifiedName.parts());
  }
}
