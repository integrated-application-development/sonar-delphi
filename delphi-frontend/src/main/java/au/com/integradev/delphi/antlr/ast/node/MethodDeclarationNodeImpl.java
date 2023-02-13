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

import au.com.integradev.delphi.antlr.DelphiLexer;
import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import au.com.integradev.delphi.symbol.declaration.TypeNameDeclaration;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;

public final class MethodDeclarationNodeImpl extends MethodNodeImpl
    implements MethodDeclarationNode {
  private Boolean isOverride;
  private Boolean isVirtual;
  private Boolean isMessage;

  private VisibilityType visibility;

  public MethodDeclarationNodeImpl(Token token) {
    super(token);
  }

  public MethodDeclarationNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public boolean isOverride() {
    if (isOverride == null) {
      isOverride = getMethodHeading().getFirstChildWithId(DelphiLexer.OVERRIDE) != null;
    }
    return isOverride;
  }

  @Override
  public boolean isVirtual() {
    if (isVirtual == null) {
      isVirtual = getMethodHeading().getFirstChildWithId(DelphiLexer.VIRTUAL) != null;
    }
    return isVirtual;
  }

  @Override
  public boolean isMessage() {
    if (isMessage == null) {
      isMessage = getMethodHeading().getFirstChildWithId(DelphiLexer.MESSAGE) != null;
    }
    return isMessage;
  }

  @Override
  protected VisibilityType createVisibility() {
    if (visibility == null) {
      if (jjtGetParent() instanceof VisibilitySectionNode) {
        visibility = ((VisibilitySectionNode) jjtGetParent()).getVisibility();
      } else {
        visibility = VisibilityType.PUBLIC;
      }
    }
    return visibility;
  }

  @Override
  @Nullable
  public TypeNameDeclaration getTypeDeclaration() {
    TypeDeclarationNode typeDeclaration = getFirstParentOfType(TypeDeclarationNode.class);
    if (typeDeclaration != null) {
      return typeDeclaration.getTypeNameDeclaration();
    }
    return null;
  }
}
