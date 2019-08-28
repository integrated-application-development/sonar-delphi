package org.sonar.plugins.delphi.symbol;

import net.sourceforge.pmd.lang.symboltable.AbstractNameDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;

public abstract class DelphiNameDeclaration extends AbstractNameDeclaration {

  private final DelphiScope scope;

  DelphiNameDeclaration(@NotNull DelphiNode node) {
    this(node, node.getScope());
  }

  DelphiNameDeclaration(@Nullable DelphiNode node, @NotNull DelphiScope scope) {
    super(node);
    this.scope = scope;
  }

  @Override
  public DelphiNode getNode() {
    return (DelphiNode) this.node;
  }

  @Override
  public final DelphiScope getScope() {
    return scope;
  }
}
