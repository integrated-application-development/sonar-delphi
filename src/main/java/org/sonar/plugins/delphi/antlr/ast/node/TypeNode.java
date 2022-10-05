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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public abstract class TypeNode extends DelphiNode implements Typed {
  private Type type;
  private List<TypeReferenceNode> parentTypeNodes;
  private Set<Type> parentTypes;

  protected TypeNode(Token token) {
    super(token);
  }

  protected TypeNode(int tokenType) {
    super(tokenType);
  }

  private AncestorListNode getAncestorListNode() {
    Node child = jjtGetChild(0);
    return child instanceof AncestorListNode ? (AncestorListNode) child : null;
  }

  public final List<TypeReferenceNode> getParentTypeNodes() {
    if (parentTypeNodes == null) {
      AncestorListNode parentsNode = getAncestorListNode();
      parentTypeNodes =
          parentsNode != null
              ? parentsNode.findChildrenOfType(TypeReferenceNode.class)
              : Collections.emptyList();
    }
    return parentTypeNodes;
  }

  public final Set<Type> getParentTypes() {
    if (parentTypes == null) {
      parentTypes =
          getParentTypeNodes().stream()
              .map(TypeReferenceNode::getType)
              .collect(Collectors.toUnmodifiableSet());
    }
    return parentTypes;
  }

  @Override
  public final String getImage() {
    return getType().getImage();
  }

  @Override
  @NotNull
  public final Type getType() {
    if (type == null) {
      type = createType();
    }
    return type;
  }

  @NotNull
  protected abstract Type createType();

  public void clearCachedType() {
    this.type = null;
  }
}
