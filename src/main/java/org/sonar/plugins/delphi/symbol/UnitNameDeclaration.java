package org.sonar.plugins.delphi.symbol;

import org.sonar.plugins.delphi.antlr.ast.node.FileHeaderNode;

public final class UnitNameDeclaration extends DelphiNameDeclaration {
  public static final String UNKNOWN_UNIT = "<unknown unit>";
  private final UnitScope unitScope;
  private int hashCode;

  public UnitNameDeclaration(FileHeaderNode node, UnitScope unitScope) {
    super(node.getNameNode(), unitScope);
    this.unitScope = unitScope;
  }

  public UnitScope getUnitScope() {
    return unitScope;
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
