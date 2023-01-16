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

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.communitydelphi.antlr.DelphiLexer;
import org.sonar.plugins.communitydelphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.communitydelphi.type.DelphiType;
import org.sonar.plugins.communitydelphi.type.Type;
import org.sonar.plugins.communitydelphi.type.Typed;

public final class FormalParameterNode extends DelphiNode implements Typed {
  private List<FormalParameterData> parameters;

  public FormalParameterNode(Token token) {
    super(token);
  }

  public FormalParameterNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<FormalParameterData> getParameters() {
    if (parameters == null) {
      NameDeclarationListNode identifierList = (NameDeclarationListNode) jjtGetChild(0);
      Type type = getType();
      ExpressionNode defaultValue = getDefaultValue();

      parameters =
          identifierList.getDeclarations().stream()
              .map(
                  identifier ->
                      new FormalParameterData(
                          identifier, type, defaultValue, isOut(), isVar(), isConst()))
              .collect(Collectors.toList());
    }
    return parameters;
  }

  @Nullable
  public TypeNode getTypeNode() {
    Node typeNode = jjtGetChild(1);
    return (typeNode instanceof TypeNode) ? (TypeNode) typeNode : null;
  }

  @Override
  @NotNull
  public Type getType() {
    TypeNode typeNode = getTypeNode();
    return (typeNode == null) ? DelphiType.untypedType() : typeNode.getType();
  }

  private ExpressionNode getDefaultValue() {
    return getFirstChildOfType(ExpressionNode.class);
  }

  private boolean isOut() {
    return getFirstChildWithId(DelphiLexer.OUT) != null;
  }

  private boolean isVar() {
    return getFirstChildWithId(DelphiLexer.VAR) != null;
  }

  private boolean isConst() {
    return getFirstChildWithId(DelphiLexer.CONST) != null;
  }

  public static class FormalParameterData implements Typed {
    private final NameDeclarationNode node;
    private final Type type;
    private final ExpressionNode defaultValue;
    private final boolean isOut;
    private final boolean isVar;
    private final boolean isConst;

    private FormalParameterData(
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

    public NameDeclarationNode getNode() {
      return node;
    }

    @Override
    @NotNull
    public Type getType() {
      return type;
    }

    public String getImage() {
      return node.getImage();
    }

    public boolean hasDefaultValue() {
      return defaultValue != null;
    }

    public ExpressionNode getDefaultValue() {
      return defaultValue;
    }

    public boolean isOut() {
      return isOut;
    }

    public boolean isVar() {
      return isVar;
    }

    public boolean isConst() {
      return isConst;
    }
  }
}
