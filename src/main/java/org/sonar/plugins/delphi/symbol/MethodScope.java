package org.sonar.plugins.delphi.symbol;

import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;

public class MethodScope extends AbstractDelphiScope {

  private final MethodImplementationNode node;
  private final DelphiScope typeScope;

  public MethodScope(MethodImplementationNode node) {
    this.node = node;
    this.typeScope = node.getTypeScope();
  }

  public DelphiScope getTypeScope() {
    return typeScope;
  }

  public String getName() {
    return node.getMethodName().fullyQualifiedName();
  }

  @Override
  public String toString() {
    return getName() + " <MethodScope>:" + glomNames(getVariableDeclarations().keySet());
  }
}
