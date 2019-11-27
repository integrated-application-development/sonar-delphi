package org.sonar.plugins.delphi.symbol;

import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;

public interface ImportResolutionHandler
    extends Function<UnitImportNode, UnitImportNameDeclaration> {
  @NotNull
  @Override
  UnitImportNameDeclaration apply(UnitImportNode importNode);
}
