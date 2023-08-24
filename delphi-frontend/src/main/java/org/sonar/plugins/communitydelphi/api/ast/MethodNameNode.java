/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.Qualifiable;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;

public interface MethodNameNode extends DelphiNode, Qualifiable {
  NameReferenceNode getNameReferenceNode();

  SimpleNameDeclarationNode getNameDeclarationNode();

  @Nullable
  MethodNameDeclaration getMethodNameDeclaration();

  String simpleNameWithTypeParameters();

  void setMethodNameDeclaration(MethodNameDeclaration declaration);

  List<NameOccurrence> getUsages();
}
