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
package org.sonar.plugins.communitydelphi.api.symbol.declaration;

import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.TypeSpecializationContext;

public interface NameDeclaration extends Comparable<NameDeclaration> {
  Node getNode();

  String getImage();

  DelphiScope getScope();

  String getName();

  /**
   * If applicable, returns a new declaration with any relevant generic types specialized. Also
   * attaches a reference to the original generic declaration.
   *
   * @param context information about the type arguments and parameters
   * @return specialized declaration
   */
  NameDeclaration specialize(TypeSpecializationContext context);

  NameDeclaration getGenericDeclaration();

  @Nullable
  NameDeclaration getForwardDeclaration();

  boolean isSpecializedDeclaration();

  boolean isForwardDeclaration();

  boolean isImplementationDeclaration();
}
