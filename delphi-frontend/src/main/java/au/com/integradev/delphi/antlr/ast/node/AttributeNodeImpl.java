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
import au.com.integradev.delphi.symbol.occurrence.AttributeNameOccurrenceImpl;
import com.google.common.base.Suppliers;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.AttributeNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;

public final class AttributeNodeImpl extends DelphiNodeImpl implements AttributeNode {
  private final Supplier<NameReferenceNode> nameReferenceSupplier =
      Suppliers.memoize(this::findNameReference);
  private final Supplier<ArgumentListNode> argumentListSupplier =
      Suppliers.memoize(this::findArgumentList);

  public AttributeNodeImpl(Token token) {
    super(token);
  }

  public AttributeNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public boolean isAssembly() {
    return getChild(0).getTokenType() == DelphiTokenType.ASSEMBLY;
  }

  @Override
  public ExpressionNode getExpression() {
    return (ExpressionNode) getChild(isAssembly() ? 1 : 0);
  }

  @Nullable
  @Override
  public NameReferenceNode getNameReference() {
    return nameReferenceSupplier.get();
  }

  @Override
  public ArgumentListNode getArgumentList() {
    return argumentListSupplier.get();
  }

  @Override
  public NameOccurrence getTypeNameOccurrence() {
    NameReferenceNode nameReference = getNameReference();
    if (nameReference != null) {
      NameOccurrence occurrence = nameReference.getLastName().getNameOccurrence();
      if (occurrence instanceof AttributeNameOccurrenceImpl) {
        return occurrence;
      }
    }
    return null;
  }

  @Override
  public NameOccurrence getConstructorNameOccurrence() {
    NameOccurrence occurrence = getTypeNameOccurrence();
    if (occurrence != null) {
      return ((AttributeNameOccurrenceImpl) occurrence).getImplicitConstructorNameOccurrence();
    }
    return null;
  }

  @Override
  public String getImage() {
    String image = getExpression().getImage();
    if (isAssembly()) {
      image = "assembly : " + image;
    }
    return image;
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  private NameReferenceNode findNameReference() {
    ExpressionNode expression = getExpression();
    if (expression instanceof PrimaryExpressionNode) {
      DelphiNode child = expression.getChild(0);
      if (child instanceof NameReferenceNode) {
        return (NameReferenceNode) child;
      }
    }
    return null;
  }

  private ArgumentListNode findArgumentList() {
    NameReferenceNode nameReference = getNameReference();
    if (nameReference != null) {
      int index = nameReference.getChildIndex() + 1;
      DelphiNode next = nameReference.getParent().getChild(index);
      if (next instanceof ArgumentListNode) {
        return (ArgumentListNode) next;
      }
    }
    return null;
  }
}
