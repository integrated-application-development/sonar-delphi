package org.sonar.plugins.delphi.symbol;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;
import org.sonar.plugins.delphi.symbol.declaration.UnitImportNameDeclaration;

@FunctionalInterface
public interface ImportResolutionHandler {
  @NotNull
  UnitImportNameDeclaration resolveImport(String namespace, UnitImportNode importNode);
}
