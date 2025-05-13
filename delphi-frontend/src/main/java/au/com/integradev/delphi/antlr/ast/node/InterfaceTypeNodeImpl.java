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
import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.AttributeGroupNode;
import org.sonar.plugins.communitydelphi.api.ast.AttributeListNode;
import org.sonar.plugins.communitydelphi.api.ast.AttributeNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceGuidNode;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.VisibilitySectionNode;

public final class InterfaceTypeNodeImpl extends StructTypeNodeImpl implements InterfaceTypeNode {
  private final Supplier<ExpressionNode> guid = Suppliers.memoize(this::findGuidExpression);

  public InterfaceTypeNodeImpl(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public boolean isForwardDeclaration() {
    return getChildren().isEmpty();
  }

  @SuppressWarnings("removal")
  @Override
  public InterfaceGuidNode getGuid() {
    return null;
  }

  @Nullable
  @Override
  public ExpressionNode getGuidExpression() {
    return guid.get();
  }

  private ExpressionNode findGuidExpression() {
    AttributeListNode attributeList = findFirstAttributeList();
    if (attributeList != null) {
      AttributeGroupNode attributeGroup = attributeList.getAttributeGroups().get(0);
      AttributeNode attribute = Iterables.getLast(attributeGroup.getAttributes());
      ExpressionNode expression = attribute.getExpression();
      if (expression.getType().isString()) {
        return expression;
      }
    }
    return null;
  }

  private AttributeListNode findFirstAttributeList() {
    for (DelphiNode child : getChildren()) {
      if (child instanceof AttributeListNode) {
        return (AttributeListNode) child;
      }

      if (child instanceof VisibilitySectionNode) {
        return findFirstAttributeList((VisibilitySectionNode) child);
      }
    }
    return null;
  }

  private AttributeListNode findFirstAttributeList(VisibilitySectionNode visibilitySection) {
    for (DelphiNode child : visibilitySection.getChildren()) {
      if (child instanceof RoutineDeclarationNode) {
        return ((RoutineDeclarationNode) child).getRoutineHeading().getAttributeList();
      }

      if (child instanceof PropertyNode) {
        return ((PropertyNode) child).getAttributeList();
      }
    }
    return null;
  }
}
