/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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

import au.com.integradev.delphi.antlr.ast.node.utils.RoutineDirectiveUtils;
import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import java.util.Set;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodHeadingNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineParametersNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineReturnTypeNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineKind;

public final class AnonymousMethodHeadingNodeImpl extends DelphiNodeImpl
    implements AnonymousMethodHeadingNode {
  private RoutineKind routineKind;
  private Set<RoutineDirective> directives;

  public AnonymousMethodHeadingNodeImpl(Token token) {
    super(token);
  }

  public AnonymousMethodHeadingNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public RoutineParametersNode getRoutineParametersNode() {
    return getFirstChildOfType(RoutineParametersNode.class);
  }

  @Override
  public RoutineReturnTypeNode getReturnTypeNode() {
    return getFirstChildOfType(RoutineReturnTypeNode.class);
  }

  @Override
  public RoutineKind getRoutineKind() {
    if (routineKind == null) {
      routineKind = RoutineKind.fromTokenType(getChild(0).getTokenType());
    }
    return routineKind;
  }

  @Override
  public Set<RoutineDirective> getDirectives() {
    if (directives == null) {
      directives = RoutineDirectiveUtils.getDirectives(this);
    }
    return directives;
  }
}
