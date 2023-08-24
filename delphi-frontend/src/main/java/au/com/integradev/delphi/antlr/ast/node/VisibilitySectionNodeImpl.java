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
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.VisibilityNode;
import org.sonar.plugins.communitydelphi.api.ast.VisibilitySectionNode;

public final class VisibilitySectionNodeImpl extends DelphiNodeImpl
    implements VisibilitySectionNode {
  private VisibilityType visibilityType;

  public VisibilitySectionNodeImpl(Token token) {
    super(token);
  }

  public VisibilitySectionNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public VisibilityType getVisibility() {
    if (visibilityType == null) {
      DelphiNode child = getChild(0);
      if (child instanceof VisibilityNode) {
        visibilityType = ((VisibilityNode) child).getVisibility();
      } else {
        visibilityType = VisibilityType.IMPLICIT_PUBLISHED;
      }
    }
    return visibilityType;
  }
}
