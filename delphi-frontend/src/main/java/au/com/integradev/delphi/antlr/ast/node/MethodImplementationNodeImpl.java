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
import au.com.integradev.delphi.symbol.NameDeclaration;
import au.com.integradev.delphi.symbol.declaration.TypeNameDeclaration;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.AsmStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.BlockDeclarationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodBodyNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;

public final class MethodImplementationNodeImpl extends MethodNodeImpl
    implements MethodImplementationNode {
  private TypeNameDeclaration typeDeclaration;

  public MethodImplementationNodeImpl(Token token) {
    super(token);
  }

  public MethodImplementationNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public MethodBodyNode getMethodBody() {
    return (MethodBodyNode) jjtGetChild(1);
  }

  @Override
  @Nullable
  public BlockDeclarationSectionNode getDeclarationSection() {
    return fromBody(MethodBodyNode::getDeclarationSection);
  }

  @Override
  @Nullable
  public DelphiNode getBlock() {
    return fromBody(MethodBodyNode::getBlock);
  }

  @Override
  @Nullable
  public CompoundStatementNode getStatementBlock() {
    return fromBody(MethodBodyNode::getStatementBlock);
  }

  @Override
  @Nullable
  public AsmStatementNode getAsmBlock() {
    return fromBody(MethodBodyNode::getAsmBlock);
  }

  @Nullable
  private <T> T fromBody(Function<MethodBodyNode, T> getter) {
    MethodBodyNode body = getMethodBody();
    if (body != null) {
      return getter.apply(body);
    }
    return null;
  }

  @Override
  public boolean isEmptyMethod() {
    AsmStatementNode asmBlock = getAsmBlock();
    CompoundStatementNode statementBlock = getStatementBlock();
    return (asmBlock != null && asmBlock.isEmpty())
        || (statementBlock != null && statementBlock.isEmpty());
  }

  @Override
  @Nullable
  public TypeNameDeclaration getTypeDeclaration() {
    if (typeDeclaration == null) {
      NameReferenceNode name = findTopLevelMethod().getMethodNameNode().getNameReferenceNode();
      while (name != null) {
        NameDeclaration nameDecl = name.getNameDeclaration();
        if (nameDecl instanceof TypeNameDeclaration) {
          typeDeclaration = (TypeNameDeclaration) nameDecl;
        }
        name = name.nextName();
      }
    }
    return typeDeclaration;
  }

  private MethodImplementationNode findTopLevelMethod() {
    MethodImplementationNode result = this;
    MethodImplementationNode nextMethod;
    while ((nextMethod = result.getFirstParentOfType(MethodImplementationNode.class)) != null) {
      result = nextMethod;
    }
    return result;
  }

  @Override
  @Nonnull
  public NameReferenceNode getNameReferenceNode() {
    return getMethodNameNode().getNameReferenceNode();
  }

  @Override
  public VisibilityType createVisibility() {
    return VisibilityType.PUBLIC;
  }
}
