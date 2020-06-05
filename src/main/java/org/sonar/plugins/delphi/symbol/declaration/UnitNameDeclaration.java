package org.sonar.plugins.delphi.symbol.declaration;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.sonar.plugins.delphi.antlr.ast.node.FileHeaderNode;
import org.sonar.plugins.delphi.symbol.scope.FileScope;

public final class UnitNameDeclaration extends QualifiedDelphiNameDeclaration {
  public static final String UNKNOWN_UNIT = "<unknown unit>";
  private final FileScope unitScope;
  private final String namespace;
  private final Path path;

  private Set<UnitNameDeclaration> interfaceDependencies;
  private Set<UnitNameDeclaration> implementationDependencies;
  private int hashCode;

  public UnitNameDeclaration(FileHeaderNode node, FileScope unitScope) {
    super(node.getNameNode(), unitScope);
    this.unitScope = unitScope;
    this.namespace = node.getNamespace();
    this.path = Path.of(node.getASTTree().getFileName());
    this.interfaceDependencies = new HashSet<>();
    this.implementationDependencies = new HashSet<>();
  }

  public FileScope getUnitScope() {
    return unitScope;
  }

  public String getNamespace() {
    return namespace;
  }

  public Path getPath() {
    return path;
  }

  public void addInterfaceDependency(UnitNameDeclaration dependency) {
    interfaceDependencies.add(dependency);
  }

  public void addImplementationDependency(UnitNameDeclaration dependency) {
    implementationDependencies.add(dependency);
  }

  public Set<UnitNameDeclaration> getInterfaceDependencies() {
    return interfaceDependencies;
  }

  public Set<UnitNameDeclaration> getImplementationDependencies() {
    return implementationDependencies;
  }

  public boolean hasDependency(UnitNameDeclaration dependency) {
    return interfaceDependencies.contains(dependency)
        || implementationDependencies.contains(dependency);
  }

  @Override
  public String toString() {
    return "Unit " + getName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UnitNameDeclaration that = (UnitNameDeclaration) o;
    return getImage().equalsIgnoreCase(that.getImage()) && path.equals(that.path);
  }

  @Override
  public int hashCode() {
    if (hashCode == 0) {
      hashCode = Objects.hash(getImage().toLowerCase(), path);
    }
    return hashCode;
  }
}
