/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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
import au.com.integradev.delphi.type.generic.constraint.TypeConstraintImpl;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.TypeConstraintNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeReferenceNode;
import org.sonar.plugins.communitydelphi.api.type.Constraint;

public final class TypeConstraintNodeImpl extends DelphiNodeImpl implements TypeConstraintNode {
  private Constraint constraint;

  public TypeConstraintNodeImpl(Token token) {
    super(token);
  }

  public TypeConstraintNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public TypeReferenceNode getTypeNode() {
    return (TypeReferenceNode) getChild(0);
  }

  @Override
  public Constraint getConstraint() {
    if (constraint == null) {
      constraint = new TypeConstraintImpl(getTypeNode().getType());
    }
    return constraint;
  }
}
