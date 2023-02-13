package org.sonar.plugins.communitydelphi.api.ast;

import au.com.integradev.delphi.symbol.declaration.UnitImportNameDeclaration;

public interface UnitImportNode extends DelphiNode {
  QualifiedNameDeclarationNode getNameNode();

  boolean isResolvedImport();

  UnitImportNameDeclaration getImportNameDeclaration();
}
