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
import java.util.Set;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodKind;
import org.sonar.plugins.communitydelphi.api.type.Type;

public interface MethodHeadingNode extends DelphiNode {
  MethodNameNode getMethodNameNode();

  MethodParametersNode getMethodParametersNode();

  List<FormalParameterData> getParameters();

  List<Type> getParameterTypes();

  MethodReturnTypeNode getMethodReturnType();

  boolean isClassMethod();

  String simpleName();

  String fullyQualifiedName();

  String getTypeName();

  boolean isMethodImplementation();

  boolean isMethodDeclaration();

  MethodKind getMethodKind();

  Set<MethodDirective> getDirectives();
}
