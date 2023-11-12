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
import javax.annotation.Nonnull;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.IntegerLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.StrongAliasTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;
import org.sonar.plugins.communitydelphi.api.type.CodePages;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;

public final class StrongAliasTypeNodeImpl extends TypeNodeImpl implements StrongAliasTypeNode {
  public StrongAliasTypeNodeImpl(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public TypeNode getAliasedTypeNode() {
    return (TypeNode) getChild(0);
  }

  @Override
  public ExpressionNode getCodePageExpression() {
    return (ExpressionNode) getChild(1);
  }

  @Override
  @Nonnull
  protected Type createType() {
    TypeDeclarationNode typeDeclaration = (TypeDeclarationNode) getParent();
    String typeName = typeDeclaration.fullyQualifiedName();

    Type aliasedType = getAliasedTypeNode().getType();
    ExpressionNode codePageExpression = getCodePageExpression();

    if (aliasedType.is(IntrinsicType.ANSISTRING) && codePageExpression != null) {
      int codePage = extractCodePage(codePageExpression);
      aliasedType = getTypeFactory().ansiString(codePage);
    }

    return getTypeFactory().strongAlias(typeName, aliasedType);
  }

  private static int extractCodePage(ExpressionNode expression) {
    IntegerLiteralNode codePage = ExpressionNodeUtils.unwrapInteger(expression);
    if (codePage != null) {
      return codePage.getValue().intValue();
    }
    return CodePages.CP_ACP;
  }
}
