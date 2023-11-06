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
package au.com.integradev.delphi.symbol.scope;

import static java.util.function.Predicate.not;

import au.com.integradev.delphi.antlr.ast.node.ArrayAccessorNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.MethodNameNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.MutableDelphiNode;
import au.com.integradev.delphi.antlr.ast.node.NameDeclarationNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.NameReferenceNodeImpl;
import com.google.common.collect.Iterables;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.api.ast.ArrayAccessorNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodNameNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.FileScope;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.HelperType;

public abstract class FileScopeImpl extends DelphiScopeImpl implements FileScope {
  private final String name;
  private final Deque<FileScope> imports = new ArrayDeque<>();
  private Map<Integer, DelphiScope> registeredScopes = new HashMap<>();
  private Map<Integer, NameDeclaration> registeredDeclarations = new HashMap<>();
  private Map<Integer, NameOccurrence> registeredOccurrences = new HashMap<>();

  protected FileScopeImpl(String name) {
    this.name = name;
  }

  @Override
  public Set<NameDeclaration> findDeclaration(NameOccurrence occurrence) {
    Set<NameDeclaration> result = super.findDeclaration(occurrence);
    for (FileScope importScope : imports) {
      if (result.isEmpty()) {
        result = importScope.shallowFindDeclaration(occurrence);
      } else {
        ((DelphiScopeImpl) importScope).findMethodOverloads(occurrence, result);
      }
    }
    return result;
  }

  @Override
  public HelperType getHelperForType(Type type) {
    HelperType result = super.getHelperForType(type);
    if (result == null) {
      for (FileScope importScope : imports) {
        result = importScope.shallowGetHelperForType(type);
        if (result != null) {
          break;
        }
      }
    }
    return result;
  }

  @Override
  public HelperType shallowGetHelperForType(Type type) {
    return findHelper(type);
  }

  @Override
  public Set<NameDeclaration> shallowFindDeclaration(NameOccurrence occurrence) {
    return super.findDeclaration(occurrence).stream()
        .map(NameDeclaration.class::cast)
        .filter(not(NameDeclaration::isImplementationDeclaration))
        .filter(not(UnitImportNameDeclaration.class::isInstance))
        .collect(Collectors.toSet());
  }

  @Override
  public void addDeclaration(NameDeclaration declaration) {
    if (declaration instanceof UnitImportNameDeclaration) {
      FileScope scope = ((UnitImportNameDeclaration) declaration).getUnitScope();
      if (scope != null) {
        imports.addFirst(scope);
      }
    }
    super.addDeclaration(declaration);
  }

  @Override
  protected boolean overloadsRequireOverloadDirective() {
    return true;
  }

  public String getName() {
    return name;
  }

  protected void addImport(FileScope scope) {
    this.imports.addFirst(scope);
  }

  /**
   * Registers a node as being associated with a scope so it can be re-attached later
   *
   * @param node The node which we are registering
   * @param scope The scope we want to associate the node to
   */
  public void registerScope(Node node, DelphiScope scope) {
    registeredScopes.put(node.getTokenIndex(), scope);
  }

  /**
   * Registers a node as being associated with a declaration so it can be re-attached later
   *
   * @param node The node which we want to associate the declaration with
   * @param declaration The declaration we are registering
   */
  public void registerDeclaration(Node node, NameDeclaration declaration) {
    registeredDeclarations.put(node.getTokenIndex(), declaration);
  }

  /**
   * Registers a node as being associated with an occurrence so it can be re-attached later
   *
   * @param node The node which we want to associate the name occurrence with
   * @param occurrence The occurrence we are registering
   */
  public void registerOccurrence(Node node, NameOccurrence occurrence) {
    registeredOccurrences.put(node.getTokenIndex(), occurrence);
  }

  /**
   * Attaches scope information to a particular node
   *
   * @param node The node which we want to attach symbol information to
   */
  public void attach(MutableDelphiNode node) {
    node.setScope(registeredScopes.get(node.getTokenIndex()));
  }

  /**
   * Attaches symbol declaration information to a particular node
   *
   * @param node The node which we want to attach symbol information to
   */
  public void attach(NameDeclarationNode node) {
    ((NameDeclarationNodeImpl) node)
        .setNameDeclaration(registeredDeclarations.get(node.getTokenIndex()));
  }

  /**
   * Attaches symbol declaration information to a method name node
   *
   * @param node The node which we want to attach symbol information to
   */
  public void attach(MethodNameNode node) {
    var declaration = (MethodNameDeclaration) registeredDeclarations.get(node.getTokenIndex());
    ((MethodNameNodeImpl) node).setMethodNameDeclaration(declaration);
  }

  /** Removes all scope registrations */
  public void unregisterScopes() {
    registeredScopes = new HashMap<>(0);
  }

  /** Removes all name declaration registrations */
  public void unregisterDeclarations() {
    registeredDeclarations = new HashMap<>(0);
  }

  /** Removes all name occurrence registrations */
  public void unregisterOccurrences() {
    registeredOccurrences = new HashMap<>(0);
  }

  /**
   * Attaches symbol occurrence information to a name reference node
   *
   * @param node The node which we want to attach symbol information to
   */
  public void attach(NameReferenceNode node) {
    ((NameReferenceNodeImpl) node)
        .setNameOccurrence(registeredOccurrences.get(node.getTokenIndex()));
  }

  /**
   * Attaches symbol occurrence information to an array accessor node
   *
   * @param node The node which we want to attach symbol information to
   */
  public void attach(ArrayAccessorNode node) {
    ((ArrayAccessorNodeImpl) node)
        .setImplicitNameOccurrence(registeredOccurrences.get(node.getTokenIndex()));
  }

  @Override
  public UnitNameDeclaration getUnitDeclaration() {
    return Iterables.getLast(getUnitDeclarations());
  }
}
