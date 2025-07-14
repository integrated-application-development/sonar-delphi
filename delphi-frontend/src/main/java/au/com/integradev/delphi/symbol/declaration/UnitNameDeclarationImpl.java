/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.symbol.declaration;

import au.com.integradev.delphi.symbol.SymbolicNode;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.sonar.plugins.communitydelphi.api.ast.FileHeaderNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.FileScope;

public final class UnitNameDeclarationImpl extends QualifiedNameDeclarationImpl
    implements UnitNameDeclaration {
  public static final String UNKNOWN_UNIT = "<unknown unit>";
  private final FileScope fileScope;
  private final String namespace;
  private final Path path;

  private final Set<UnitNameDeclaration> interfaceDependencies;
  private final Set<UnitNameDeclaration> implementationDependencies;
  private int hashCode;

  public UnitNameDeclarationImpl(FileHeaderNode node, FileScope fileScope, Path path) {
    super(node.getNameNode(), fileScope);
    this.fileScope = fileScope;
    this.namespace = node.getNamespace();
    this.path = path;
    this.interfaceDependencies = new HashSet<>();
    this.implementationDependencies = new HashSet<>();
  }

  public UnitNameDeclarationImpl(String image, FileScope fileScope, Path path) {
    super(SymbolicNode.imaginary(image, fileScope));
    this.fileScope = fileScope;
    this.namespace = "";
    this.path = path;
    this.interfaceDependencies = new HashSet<>();
    this.implementationDependencies = new HashSet<>();
  }

  @Override
  public FileScope getFileScope() {
    return fileScope;
  }

  @Override
  public String getNamespace() {
    return namespace;
  }

  @Override
  public Path getPath() {
    return path;
  }

  public void addInterfaceDependency(UnitNameDeclaration dependency) {
    interfaceDependencies.add(dependency);
  }

  public void addImplementationDependency(UnitNameDeclaration dependency) {
    implementationDependencies.add(dependency);
  }

  @Override
  public Set<UnitNameDeclaration> getInterfaceDependencies() {
    return interfaceDependencies;
  }

  @Override
  public Set<UnitNameDeclaration> getImplementationDependencies() {
    return implementationDependencies;
  }

  @Override
  public boolean hasDependency(UnitNameDeclaration dependency) {
    return interfaceDependencies.contains(dependency)
        || implementationDependencies.contains(dependency);
  }

  @Override
  public boolean equals(Object other) {
    if (super.equals(other)) {
      UnitNameDeclarationImpl that = (UnitNameDeclarationImpl) other;
      return path.equals(that.path);
    }
    return false;
  }

  @Override
  public int hashCode() {
    if (hashCode == 0) {
      hashCode = Objects.hash(super.hashCode(), path);
    }
    return hashCode;
  }

  @Override
  public int compareTo(@Nonnull NameDeclaration other) {
    int result = super.compareTo(other);
    if (result == 0) {
      UnitNameDeclarationImpl that = (UnitNameDeclarationImpl) other;
      result = path.compareTo(that.path);
    }
    return result;
  }

  @Override
  public String toString() {
    return "Unit " + getName();
  }
}
