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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Typed;

public interface FormalParameterNode extends DelphiNode, Typed {
  List<FormalParameterData> getParameters();

  @Nullable
  TypeNode getTypeNode();

  List<AttributeListNode> getAttributeLists();

  ExpressionNode getDefaultValue();

  boolean isOut();

  boolean isVar();

  boolean isConst();

  interface FormalParameterData extends Typed {
    NameDeclarationNode getNode();

    @Override
    @Nonnull
    Type getType();

    String getImage();

    boolean hasDefaultValue();

    ExpressionNode getDefaultValue();

    boolean isOut();

    boolean isVar();

    boolean isConst();
  }
}
