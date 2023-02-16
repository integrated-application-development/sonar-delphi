package org.sonar.plugins.communitydelphi.api.symbol.declaration;

import java.nio.file.Path;
import java.util.Set;
import org.sonar.plugins.communitydelphi.api.symbol.scope.FileScope;

public interface UnitNameDeclaration extends QualifiedNameDeclaration {

  FileScope getFileScope();

  String getNamespace();

  Path getPath();

  Set<UnitNameDeclaration> getInterfaceDependencies();

  Set<UnitNameDeclaration> getImplementationDependencies();

  boolean hasDependency(UnitNameDeclaration dependency);
}
