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
package org.sonar.plugins.communitydelphi.antlr.ast.node;

import java.util.stream.Collectors;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.communitydelphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.communitydelphi.type.Type;
import org.sonar.plugins.communitydelphi.type.parameter.FormalParameter;

public final class ProcedureTypeNode extends ProceduralTypeNode {
  public ProcedureTypeNode(Token token) {
    super(token);
  }

  public ProcedureTypeNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  @NotNull
  public Type createType() {
    return getTypeFactory()
        .procedure(
            getParameters().stream()
                .map(FormalParameter::create)
                .collect(Collectors.toUnmodifiableList()),
            getReturnType());
  }
}
