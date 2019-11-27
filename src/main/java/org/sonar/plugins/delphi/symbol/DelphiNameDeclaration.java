package org.sonar.plugins.delphi.symbol;

import net.sourceforge.pmd.lang.symboltable.AbstractNameDeclaration;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;

public abstract class DelphiNameDeclaration extends AbstractNameDeclaration {

  DelphiNameDeclaration(DelphiNode node) {
    this(node, node.getScope());
  }

  DelphiNameDeclaration(DelphiNode node, DelphiScope scope) {
    super(new SymbolicNode(node, scope));
  }

  DelphiNameDeclaration(SymbolicNode node) {
    super(node);
  }

  @Override
  public SymbolicNode getNode() {
    return (SymbolicNode) this.node;
  }

  @Override
  public DelphiScope getScope() {
    return (DelphiScope) super.getScope();
  }
}
