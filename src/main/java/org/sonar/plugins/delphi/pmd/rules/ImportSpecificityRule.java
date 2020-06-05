package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.ImplementationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;

public class ImportSpecificityRule extends AbstractImportRule {
  @Override
  public RuleContext visit(ImplementationSectionNode section, RuleContext data) {
    return data;
  }

  @Override
  protected boolean isViolation(UnitImportNode unitImport) {
    UnitNameDeclaration dependency = unitImport.getImportNameDeclaration().getOriginalDeclaration();
    return !getUnitDeclaration().getInterfaceDependencies().contains(dependency)
        && getUnitDeclaration().getImplementationDependencies().contains(dependency);
  }
}
