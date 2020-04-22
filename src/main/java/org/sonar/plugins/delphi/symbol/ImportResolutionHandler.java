package org.sonar.plugins.delphi.symbol;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;
import org.sonar.plugins.delphi.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;

@FunctionalInterface
public interface ImportResolutionHandler {
  @NotNull
  UnitImportNameDeclaration resolveImport(UnitNameDeclaration unit, UnitImportNode importNode);
}
