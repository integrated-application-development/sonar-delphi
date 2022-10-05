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

import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.getLast;

import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;

public final class EnumTypeNode extends TypeNode {
  public EnumTypeNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<EnumElementNode> getElements() {
    return findChildrenOfType(EnumElementNode.class);
  }

  @Override
  @NotNull
  public Type createType() {
    Node parent = jjtGetParent();
    String image;

    if (parent instanceof TypeDeclarationNode) {
      image = ((TypeDeclarationNode) parent).fullyQualifiedName();
    } else {
      image = makeAnonymousImage(this);
    }

    return getTypeFactory().enumeration(image, getScope());
  }

  private static String makeAnonymousImage(EnumTypeNode typeNode) {
    List<EnumElementNode> elements = typeNode.getElements();
    EnumElementNode first = getFirst(elements, null);
    if (first == null) {
      return "Enumeration";
    }

    return "Enumeration(" + first.getImage() + ".." + getLast(elements).getImage();
  }
}
