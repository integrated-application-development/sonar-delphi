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

import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class ProcedureTypeHeadingNode extends DelphiNode {
  public ProcedureTypeHeadingNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public String getImage() {
    return super.getImage() + getParameterSignature();
  }

  private String getParameterSignature() {
    MethodParametersNode parameters = getMethodParametersNode();
    return parameters != null ? parameters.getImage() : "";
  }

  @Nullable
  public MethodParametersNode getMethodParametersNode() {
    Node node = jjtGetChild(0);
    return (node instanceof MethodParametersNode) ? (MethodParametersNode) node : null;
  }

  @Nullable
  public MethodReturnTypeNode getMethodReturnTypeNode() {
    Node node = jjtGetChild(hasMethodParametersNode() ? 1 : 0);
    return (node instanceof MethodReturnTypeNode) ? (MethodReturnTypeNode) node : null;
  }

  public boolean hasMethodParametersNode() {
    return getMethodParametersNode() != null;
  }
}
