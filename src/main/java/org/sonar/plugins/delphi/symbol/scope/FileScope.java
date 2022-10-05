/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package org.sonar.plugins.delphi.symbol.scope;

import java.util.Set;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.sonar.plugins.delphi.antlr.ast.node.ArrayAccessorNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.IndexedNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNameNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.HelperType;

public interface FileScope extends DelphiScope {

  /**
   * Find declarations in this scope without traversing imports or looking in the implementation
   * section.
   *
   * @param occurrence The name for which we are trying to find a matching declaration
   * @return Set of name declarations matching the name occurrence
   */
  Set<NameDeclaration> shallowFindDeclaration(DelphiNameOccurrence occurrence);

  /**
   * Find a helper type in this scope without traversing imports
   *
   * @param type The type for which we are trying to find a helper
   * @return Helper type for the specified type
   */
  @Nullable
  HelperType shallowGetHelperForType(Type type);

  /**
   * Registers a node as being associated with a scope so it can be re-attached later
   *
   * @param node The node which we are registering
   * @param scope The scope we want to associate the node to
   */
  void registerScope(IndexedNode node, DelphiScope scope);

  /**
   * Registers a node as being associated with a declaration so it can be re-attached later
   *
   * @param node The node which we want to associate the declaration with
   * @param declaration The declaration we are registering
   */
  void registerDeclaration(IndexedNode node, NameDeclaration declaration);

  /**
   * Registers a node as being associated with an occurrence so it can be re-attached later
   *
   * @param node The node which we want to associate the name occurrence with
   * @param occurrence The occurrence we are registering
   */
  void registerOccurrence(IndexedNode node, NameOccurrence occurrence);

  /**
   * Attaches scope information to a particular node
   *
   * @param node The node which we want to attach symbol information to
   */
  void attach(DelphiNode node);

  /**
   * Attaches symbol declaration information to a particular node
   *
   * @param node The node which we want to attach symbol information to
   */
  void attach(NameDeclarationNode node);

  /**
   * Attaches symbol declaration information to a method name node
   *
   * @param node The node which we want to attach symbol information to
   */
  void attach(MethodNameNode node);

  /** Removes all scope registrations */
  void unregisterScopes();

  /** Removes all name declaration registrations */
  void unregisterDeclarations();

  /** Removes all name occurrence registrations */
  void unregisterOccurrences();

  /**
   * Attaches symbol occurrence information to a name reference node
   *
   * @param node The node which we want to attach symbol information to
   */
  void attach(NameReferenceNode node);

  /**
   * Attaches symbol occurrence information to an array accessor node
   *
   * @param node The node which we want to attach symbol information to
   */
  void attach(ArrayAccessorNode node);

  /**
   * Returns the system scope
   *
   * @return System scope
   */
  SystemScope getSystemScope();

  /**
   * Returns the declaration representing this file
   *
   * @return Unit name declaration
   */
  UnitNameDeclaration getUnitDeclaration();
}
