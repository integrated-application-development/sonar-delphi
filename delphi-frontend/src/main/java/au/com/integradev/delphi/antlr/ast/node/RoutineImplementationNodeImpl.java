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
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.AsmStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.LocalDeclarationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineBodyNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;

public final class RoutineImplementationNodeImpl extends RoutineNodeImpl
    implements RoutineImplementationNode {
  private TypeNameDeclaration typeDeclaration;

  public RoutineImplementationNodeImpl(Token token) {
    super(token);
  }

  public RoutineImplementationNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public RoutineBodyNode getRoutineBody() {
    return (RoutineBodyNode) getChild(1);
  }

  @Override
  @Nullable
  public LocalDeclarationSectionNode getDeclarationSection() {
    return fromBody(RoutineBodyNode::getDeclarationSection);
  }

  @Override
  @Nullable
  public DelphiNode getBlock() {
    return fromBody(RoutineBodyNode::getBlock);
  }

  @Override
  @Nullable
  public CompoundStatementNode getStatementBlock() {
    return fromBody(RoutineBodyNode::getStatementBlock);
  }

  @Override
  @Nullable
  public AsmStatementNode getAsmBlock() {
    return fromBody(RoutineBodyNode::getAsmBlock);
  }

  @Nullable
  private <T> T fromBody(Function<RoutineBodyNode, T> getter) {
    RoutineBodyNode body = getRoutineBody();
    if (body != null) {
      return getter.apply(body);
    }
    return null;
  }

  @Override
  public boolean isEmpty() {
    AsmStatementNode asmBlock = getAsmBlock();
    CompoundStatementNode statementBlock = getStatementBlock();
    return (asmBlock != null && asmBlock.isEmpty())
        || (statementBlock != null && statementBlock.isEmpty());
  }

  @Override
  @Nullable
  public TypeNameDeclaration getTypeDeclaration() {
    if (typeDeclaration == null) {
      NameReferenceNode name = findTopLevelRoutine().getRoutineNameNode().getNameReferenceNode();
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

  private RoutineImplementationNode findTopLevelRoutine() {
    RoutineImplementationNode result = this;
    RoutineImplementationNode nextRoutine;
    while ((nextRoutine = result.getFirstParentOfType(RoutineImplementationNode.class)) != null) {
      result = nextRoutine;
    }
    return result;
  }

  @Override
  @Nonnull
  public NameReferenceNode getNameReferenceNode() {
    return getRoutineNameNode().getNameReferenceNode();
  }

  @Override
  public VisibilityType createVisibility() {
    return VisibilityType.PUBLIC;
  }
}
