package org.sonar.plugins.delphi.symbol.declaration;

import com.google.common.collect.ComparisonChain;
import java.util.Objects;
import net.sourceforge.pmd.lang.symboltable.AbstractNameDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.type.TypeSpecializationContext;

public abstract class AbstractDelphiNameDeclaration extends AbstractNameDeclaration
    implements DelphiNameDeclaration {
  private DelphiNameDeclaration genericDeclaration;
  private int hashcode;
  private boolean isForwardDeclaration;
  private DelphiNameDeclaration forwardDeclaration;

  AbstractDelphiNameDeclaration(DelphiNode node) {
    this(node, node.getScope());
  }

  AbstractDelphiNameDeclaration(DelphiNode node, DelphiScope scope) {
    this(new SymbolicNode(node, scope));
  }

  AbstractDelphiNameDeclaration(SymbolicNode node) {
    super(node);
    generateHashCode();
  }

  private void generateHashCode() {
    hashcode = Objects.hash(getImage().toLowerCase(), isForwardDeclaration);
  }

  @Override
  public SymbolicNode getNode() {
    return (SymbolicNode) this.node;
  }

  @Override
  public DelphiScope getScope() {
    return (DelphiScope) super.getScope();
  }

  @Override
  public final DelphiNameDeclaration specialize(TypeSpecializationContext context) {
    if (!context.hasSignatureMismatch()) {
      DelphiNameDeclaration specialized = doSpecialization(context);
      if (!specialized.equals(this)) {
        specialized.setGenericDeclaration(this);
        return specialized;
      }
    }
    return this;
  }

  /**
   * This is where we actually do all the work to specialize a declaration.
   *
   * @param context information about the type arguments and parameters
   * @return specialized declaration
   */
  protected DelphiNameDeclaration doSpecialization(TypeSpecializationContext context) {
    return this;
  }

  @Override
  public boolean isSpecializedDeclaration() {
    return genericDeclaration != null;
  }

  @Override
  public DelphiNameDeclaration getGenericDeclaration() {
    return genericDeclaration;
  }

  @Override
  public void setGenericDeclaration(DelphiNameDeclaration genericDeclaration) {
    this.genericDeclaration = genericDeclaration;
  }

  @Override
  @Nullable
  public DelphiNameDeclaration getForwardDeclaration() {
    return forwardDeclaration;
  }

  @Override
  public void setForwardDeclaration(DelphiNameDeclaration declaration) {
    this.forwardDeclaration = declaration;
  }

  @Override
  public void setIsForwardDeclaration() {
    this.isForwardDeclaration = true;
    generateHashCode();
  }

  @Override
  public boolean isForwardDeclaration() {
    return isForwardDeclaration;
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
    return getImage().equalsIgnoreCase(that.getImage())
        && isForwardDeclaration == that.isForwardDeclaration();
  }

  @Override
  public int hashCode() {
    return hashcode;
  }

  @Override
  public int compareTo(@NotNull DelphiNameDeclaration other) {
    return ComparisonChain.start()
        .compare(getClass().getName(), other.getClass().getName())
        .compare(getName(), other.getName(), String.CASE_INSENSITIVE_ORDER)
        .compareTrueFirst(isForwardDeclaration, other.isForwardDeclaration())
        .result();
  }
}
