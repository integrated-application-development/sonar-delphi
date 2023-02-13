package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.symbol.declaration.UnitImportNameDeclaration;

public interface UnitImportNode extends DelphiNode {
  QualifiedNameDeclarationNode getNameNode();

  boolean isResolvedImport();

  UnitImportNameDeclaration getImportNameDeclaration();
}
