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
package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import au.com.integradev.delphi.symbol.NameOccurrence;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.ArrayAccessorNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;

public final class ArrayAccessorNodeImpl extends DelphiNodeImpl implements ArrayAccessorNode {
  private NameOccurrence implicitNameOccurrence;

  public ArrayAccessorNodeImpl(Token token) {
    super(token);
  }

  public ArrayAccessorNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public List<ExpressionNode> getExpressions() {
    return findChildrenOfType(ExpressionNode.class);
  }

  @Override
  public void setImplicitNameOccurrence(NameOccurrence implicitNameOccurrence) {
    this.implicitNameOccurrence = implicitNameOccurrence;
  }

  @Override
  @Nullable
  public NameOccurrence getImplicitNameOccurrence() {
    return implicitNameOccurrence;
  }

  @Override
  public String getImage() {
    return "["
        + getExpressions().stream().map(ExpressionNode::getImage).collect(Collectors.joining(", "))
        + "]";
  }
}
