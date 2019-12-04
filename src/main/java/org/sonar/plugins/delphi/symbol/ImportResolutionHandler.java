package org.sonar.plugins.delphi.symbol;

import java.util.function.BiFunction;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;
import org.sonar.plugins.delphi.symbol.declaration.UnitImportNameDeclaration;

public interface ImportResolutionHandler
    extends BiFunction<String, UnitImportNode, UnitImportNameDeclaration> {
  @NotNull
  @Override
  UnitImportNameDeclaration apply(String namespace, UnitImportNode importNode);
}
