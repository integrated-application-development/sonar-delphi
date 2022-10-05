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
package org.sonar.plugins.delphi.type;

import org.sonar.plugins.delphi.antlr.ast.node.ClassHelperTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.ClassTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.InterfaceTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.ObjectTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.RecordHelperTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.RecordTypeNode;

public enum StructKind {
  CLASS(ClassTypeNode.class),
  CLASS_HELPER(ClassHelperTypeNode.class),
  INTERFACE(InterfaceTypeNode.class),
  OBJECT(ObjectTypeNode.class),
  RECORD(RecordTypeNode.class),
  RECORD_HELPER(RecordHelperTypeNode.class);

  private final Class<? extends DelphiNode> nodeType;

  StructKind(Class<? extends DelphiNode> nodeType) {
    this.nodeType = nodeType;
  }

  public static StructKind fromNode(DelphiNode node) {
    for (StructKind kind : StructKind.values()) {
      if (kind.nodeType == node.getClass()) {
        return kind;
      }
    }
    throw new AssertionError("Unknown StructKind. TypeNode: " + node.getClass().getSimpleName());
  }
}
