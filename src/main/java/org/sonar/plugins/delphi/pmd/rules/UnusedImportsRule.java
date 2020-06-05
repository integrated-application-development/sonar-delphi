package org.sonar.plugins.delphi.pmd.rules;

import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;

public class UnusedImportsRule extends AbstractImportRule {
  @Override
  protected boolean isViolation(UnitImportNode unitImport) {
    UnitNameDeclaration dependency = unitImport.getImportNameDeclaration().getOriginalDeclaration();
    return !getUnitDeclaration().hasDependency(dependency);
  }
}
