package org.sonar.plugins.delphi.symbol.scope;

import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;

public class MethodScope extends AbstractDelphiScope {

  private final String name;
  private final DelphiScope typeScope;

  public MethodScope(MethodImplementationNode node) {
    name = node.fullyQualifiedName();
    this.typeScope = node.getTypeScope();
  }

  public DelphiScope getTypeScope() {
    return typeScope;
  }

  @Override
  public String toString() {
    return name + " <MethodScope>:" + glomNames(getVariableDeclarations().keySet());
  }
}
