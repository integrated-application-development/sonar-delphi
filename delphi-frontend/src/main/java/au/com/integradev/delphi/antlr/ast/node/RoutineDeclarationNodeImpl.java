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
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.RoutineDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.VisibilitySectionNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;

public final class RoutineDeclarationNodeImpl extends RoutineNodeImpl
    implements RoutineDeclarationNode {
  private Boolean isOverride;
  private Boolean isVirtual;
  private Boolean isMessage;

  private VisibilityType visibility;

  public RoutineDeclarationNodeImpl(Token token) {
    super(token);
  }

  public RoutineDeclarationNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public boolean isOverride() {
    if (isOverride == null) {
      isOverride = getRoutineHeading().getFirstChildWithTokenType(DelphiTokenType.OVERRIDE) != null;
    }
    return isOverride;
  }

  @Override
  public boolean isVirtual() {
    if (isVirtual == null) {
      isVirtual = getRoutineHeading().getFirstChildWithTokenType(DelphiTokenType.VIRTUAL) != null;
    }
    return isVirtual;
  }

  @Override
  public boolean isMessage() {
    if (isMessage == null) {
      isMessage = getRoutineHeading().getFirstChildWithTokenType(DelphiTokenType.MESSAGE) != null;
    }
    return isMessage;
  }

  @Override
  protected VisibilityType createVisibility() {
    if (visibility == null) {
      if (getParent() instanceof VisibilitySectionNode) {
        visibility = ((VisibilitySectionNode) getParent()).getVisibility();
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
