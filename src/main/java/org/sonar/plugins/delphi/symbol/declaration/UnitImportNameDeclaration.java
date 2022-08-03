package org.sonar.plugins.delphi.symbol.declaration;

import java.util.Objects;
import javax.annotation.Nullable;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;
import org.sonar.plugins.delphi.symbol.scope.FileScope;

public final class UnitImportNameDeclaration extends QualifiedDelphiNameDeclaration {
  private final UnitNameDeclaration originalDeclaration;

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
  public FileScope getUnitScope() {
    if (originalDeclaration == null) {
      return null;
    }
    return originalDeclaration.getFileScope();
  }

  @Override
  public boolean equals(Object other) {
    if (super.equals(other)) {
      UnitImportNameDeclaration that = (UnitImportNameDeclaration) other;
      return Objects.equals(originalDeclaration, that.originalDeclaration);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), originalDeclaration);
  }

  @Override
  public String toString() {
    return "Import " + getName();
  }
}
