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
package au.com.integradev.delphi.antlr.ast.node;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.AncestorListNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeReferenceNode;
import org.sonar.plugins.communitydelphi.api.type.Type;

public abstract class TypeNodeImpl extends DelphiNodeImpl implements TypeNode {
  private Type type;
  private List<TypeReferenceNode> parentTypeNodes;
  private Set<Type> parentTypes;

  protected TypeNodeImpl(Token token) {
    super(token);
  }

  protected TypeNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public AncestorListNode getAncestorListNode() {
    DelphiNode child = getChild(0);
    return child instanceof AncestorListNode ? (AncestorListNode) child : null;
  }

  @Override
  public final List<TypeReferenceNode> getParentTypeNodes() {
    if (parentTypeNodes == null) {
      AncestorListNode parentsNode = getAncestorListNode();
      parentTypeNodes =
          parentsNode != null ? parentsNode.getParentTypeNodes() : Collections.emptyList();
    }
    return parentTypeNodes;
  }

  @Override
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
  @Nonnull
  public final Type getType() {
    if (type == null) {
      type = createType();
    }
    return type;
  }

  public void clearCachedType() {
    this.type = null;
  }

  protected abstract Type createType();
}
