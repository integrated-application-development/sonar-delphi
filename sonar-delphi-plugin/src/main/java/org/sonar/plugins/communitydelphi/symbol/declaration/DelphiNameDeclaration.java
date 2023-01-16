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
package org.sonar.plugins.communitydelphi.symbol.declaration;

import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.communitydelphi.symbol.SymbolicNode;
import org.sonar.plugins.communitydelphi.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.type.generic.TypeSpecializationContext;

public interface DelphiNameDeclaration extends NameDeclaration, Comparable<DelphiNameDeclaration> {

  @Override
  SymbolicNode getNode();

  @Override
  DelphiScope getScope();

  /**
   * If applicable, returns a new declaration with any relevant generic types specialized. Also
   * attaches a reference to the original generic declaration.
   *
   * @param context information about the type arguments and parameters
   * @return specialized declaration
   */
  DelphiNameDeclaration specialize(TypeSpecializationContext context);

  boolean isSpecializedDeclaration();

  DelphiNameDeclaration getGenericDeclaration();

  void setGenericDeclaration(DelphiNameDeclaration genericDeclaration);

  @Nullable
  DelphiNameDeclaration getForwardDeclaration();

  void setForwardDeclaration(DelphiNameDeclaration declaration);

  void setIsForwardDeclaration();

  boolean isForwardDeclaration();

  void setIsImplementationDeclaration();

  boolean isImplementationDeclaration();
}
