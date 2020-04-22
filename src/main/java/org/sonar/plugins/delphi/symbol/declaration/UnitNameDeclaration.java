package org.sonar.plugins.delphi.symbol.declaration;

import org.sonar.plugins.delphi.antlr.ast.node.FileHeaderNode;
import org.sonar.plugins.delphi.symbol.scope.FileScope;

public final class UnitNameDeclaration extends QualifiedDelphiNameDeclaration {
  public static final String UNKNOWN_UNIT = "<unknown unit>";
  private final FileScope unitScope;
  private final String namespace;
  private int hashCode;

  public UnitNameDeclaration(FileHeaderNode node, FileScope unitScope) {
    super(node.getNameNode(), unitScope);
    this.unitScope = unitScope;
    this.namespace = node.getNamespace();
  }

  public FileScope getUnitScope() {
    return unitScope;
  }

  public String getNamespace() {
    return namespace;
  }

  @Override
  public String toString() {
    return "Unit " + getName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UnitNameDeclaration that = (UnitNameDeclaration) o;
    return getImage().equalsIgnoreCase(that.getImage());
  }

  @Override
  public int hashCode() {
    if (hashCode == 0) {
      hashCode = getImage().toLowerCase().hashCode();
    }
    return hashCode;
  }
}
