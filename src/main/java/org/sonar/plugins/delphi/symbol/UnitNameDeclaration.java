package org.sonar.plugins.delphi.symbol;

import static org.sonar.plugins.delphi.symbol.UnknownScope.unknownScope;

import org.sonar.plugins.delphi.antlr.ast.node.FileHeaderNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;

public class UnitNameDeclaration extends DelphiNameDeclaration {
  private final DelphiScope unitScope;

  public UnitNameDeclaration(FileHeaderNode node, GlobalScope globalScope, UnitScope unitScope) {
    super(node.getNameDeclaration(), globalScope);
    this.unitScope = unitScope;
  }

  public UnitNameDeclaration(UnitImportNode node) {
    super(node.getNameDeclaration());
    this.unitScope = unknownScope();
  }

  public DelphiScope getUnitScope() {
    return unitScope;
  }
}
