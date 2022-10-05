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
package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;

public abstract class FileHeaderNode extends DelphiNode {
  private String namespace;

  FileHeaderNode(Token token) {
    super(token);
  }

  public QualifiedNameDeclarationNode getNameNode() {
    return (QualifiedNameDeclarationNode) jjtGetChild(0);
  }

  public String getName() {
    return getNameNode().fullyQualifiedName();
  }

  public String getNamespace() {
    if (namespace == null) {
      String fullName = getName();
      int dotIndex = fullName.lastIndexOf('.');
      if (dotIndex == -1) {
        namespace = "";
      } else {
        namespace = fullName.substring(0, dotIndex);
      }
    }
    return namespace;
  }
}
