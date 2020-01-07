package org.sonar.plugins.delphi.symbol.declaration;

import java.util.Objects;
import net.sourceforge.pmd.lang.symboltable.AbstractNameDeclaration;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;

public abstract class DelphiNameDeclaration extends AbstractNameDeclaration
    implements Comparable<DelphiNameDeclaration> {
  private final int hashcode;

  DelphiNameDeclaration(DelphiNode node) {
    this(node, node.getScope());
  }

  DelphiNameDeclaration(DelphiNode node, DelphiScope scope) {
    super(new SymbolicNode(node, scope));
    hashcode = Objects.hash(getImage().toLowerCase());
  }

  DelphiNameDeclaration(SymbolicNode node) {
    super(node);
    hashcode = Objects.hash(getImage().toLowerCase());
  }

  @Override
  public SymbolicNode getNode() {
    return (SymbolicNode) this.node;
  }

  @Override
  public DelphiScope getScope() {
    return (DelphiScope) super.getScope();
  }

  @SuppressWarnings("EqualsGetClass")
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DelphiNameDeclaration that = (DelphiNameDeclaration) o;
    return getImage().equalsIgnoreCase(that.getImage());
  }

  @Override
  public int hashCode() {
    return hashcode;
  }

  @Override
  public int compareTo(@NotNull DelphiNameDeclaration other) {
    if (getClass() != other.getClass()) {
      return getClass().getName().compareTo(other.getClass().getName());
    }
    return getName().compareToIgnoreCase(other.getName());
  }
}
