/*
 * Sonar Delphi Plugin
 * Copyright (C) 2026 Integrated Application Development
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
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.BinaryOperatorNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.operator.BinaryOperator;

public final class BinaryOperatorNodeImpl extends DelphiNodeImpl implements BinaryOperatorNode {
  private String image;

  public BinaryOperatorNodeImpl(Token token) {
    super(token);
  }

  public BinaryOperatorNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public BinaryOperator getOperator() {
    return BinaryOperator.fromTokenType(getTokenType());
  }

  @Override
  public String getImage() {
    if (image == null) {
      List<DelphiNode> children = getChildren();
      if (children.isEmpty()) {
        // Single-token operator
        image = getToken().getImage();
      } else {
        // Multi-token operator
        image = children.stream().map(DelphiNode::getImage).collect(Collectors.joining(" "));
      }
    }
    return image;
  }
}
