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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationListNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public final class FormalParameterNodeImpl extends DelphiNodeImpl implements FormalParameterNode {
  private List<FormalParameterData> parameters;

  public FormalParameterNodeImpl(Token token) {
    super(token);
  }

  public FormalParameterNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public List<FormalParameterData> getParameters() {
    if (parameters == null) {
      NameDeclarationListNode identifierList = (NameDeclarationListNode) getChild(0);
      Type type = getType();
      ExpressionNode defaultValue = getDefaultValue();

      parameters =
          identifierList.getDeclarations().stream()
              .map(
                  identifier ->
                      new FormalParameterDataImpl(
                          identifier, type, defaultValue, isOut(), isVar(), isConst()))
              .collect(Collectors.toList());
    }
    return parameters;
  }

  @Override
  @Nullable
  public TypeNode getTypeNode() {
    DelphiNode typeNode = getChild(1);
    return (typeNode instanceof TypeNode) ? (TypeNode) typeNode : null;
  }

  @Override
  @Nonnull
  public Type getType() {
    TypeNode typeNode = getTypeNode();
    return (typeNode == null) ? TypeFactory.untypedType() : typeNode.getType();
  }

  private static final class FormalParameterDataImpl implements FormalParameterData {
    private final NameDeclarationNode node;
    private final Type type;
    private final ExpressionNode defaultValue;
    private final boolean isOut;
    private final boolean isVar;
    private final boolean isConst;

    private FormalParameterDataImpl(
        NameDeclarationNode node,
        Type type,
        ExpressionNode defaultValue,
        boolean isOut,
        boolean isVar,
        boolean isConst) {
      this.node = node;
      this.type = type;
      this.defaultValue = defaultValue;
      this.isOut = isOut;
      this.isVar = isVar;
      this.isConst = isConst;
    }

    @Override
    public NameDeclarationNode getNode() {
      return node;
    }

    @Override
    @Nonnull
    public Type getType() {
      return type;
    }

    @Override
    public String getImage() {
      return node.getImage();
    }

    @Override
    public boolean hasDefaultValue() {
      return defaultValue != null;
    }

    @Override
    public ExpressionNode getDefaultValue() {
      return defaultValue;
    }

    @Override
    public boolean isOut() {
      return isOut;
    }

    @Override
    public boolean isVar() {
      return isVar;
    }

    @Override
    public boolean isConst() {
      return isConst;
    }
  }
}
