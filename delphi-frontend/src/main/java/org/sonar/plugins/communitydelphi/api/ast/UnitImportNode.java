package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitImportNameDeclaration;

public interface UnitImportNode extends DelphiNode {
  QualifiedNameDeclarationNode getNameNode();

  boolean isResolvedImport();

  UnitImportNameDeclaration getImportNameDeclaration();
}
