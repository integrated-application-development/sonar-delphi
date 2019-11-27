package org.sonar.plugins.delphi.symbol;

import javax.annotation.Nullable;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;

public final class UnitImportNameDeclaration extends DelphiNameDeclaration {
  private final UnitNameDeclaration originalDeclaration;
  private int hashCode;

  public UnitImportNameDeclaration(
      UnitImportNode node, @Nullable UnitNameDeclaration originalDeclaration) {
    super(node.getNameNode());
    this.originalDeclaration = originalDeclaration;
  }

  @Nullable
  public UnitNameDeclaration getOriginalDeclaration() {
    return originalDeclaration;
  }

  @Nullable
  public UnitScope getUnitScope() {
    if (originalDeclaration == null) {
      return null;
    }
    return originalDeclaration.getUnitScope();
  }

  @Override
  public String toString() {
    return "Import " + getName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UnitImportNameDeclaration that = (UnitImportNameDeclaration) o;
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
