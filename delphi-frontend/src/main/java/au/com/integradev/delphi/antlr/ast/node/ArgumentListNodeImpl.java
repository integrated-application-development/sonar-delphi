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

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;

public final class ArgumentListNodeImpl extends DelphiNodeImpl implements ArgumentListNode {
  private String image;
  private List<ArgumentNode> arguments;

  public ArgumentListNodeImpl(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @SuppressWarnings("removal")
  @Override
  public List<ExpressionNode> getArguments() {
    return getArgumentNodes().stream()
        .map(ArgumentNode::getExpression)
        .collect(Collectors.toUnmodifiableList());
  }

  @Override
  public List<ArgumentNode> getArgumentNodes() {
    if (arguments == null) {
      arguments = findChildrenOfType(ArgumentNode.class);
    }
    return arguments;
  }

  @Override
  public boolean isEmpty() {
    return getArgumentNodes().isEmpty();
  }

  @Override
  public String getImage() {
    if (image == null) {
      StringBuilder imageBuilder = new StringBuilder();
      imageBuilder.append("(");
      for (DelphiNode child : getChildren()) {
        imageBuilder.append(child.getImage());
      }
      image = imageBuilder.toString();
    }
    return image;
  }
}
