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

import static au.com.integradev.delphi.antlr.ast.node.Visibility.VisibilityType.PRIVATE;
import static au.com.integradev.delphi.antlr.ast.node.Visibility.VisibilityType.PROTECTED;
import static au.com.integradev.delphi.antlr.ast.node.Visibility.VisibilityType.PUBLIC;
import static au.com.integradev.delphi.antlr.ast.node.Visibility.VisibilityType.PUBLISHED;
import static au.com.integradev.delphi.antlr.ast.node.Visibility.VisibilityType.STRICT_PRIVATE;
import static au.com.integradev.delphi.antlr.ast.node.Visibility.VisibilityType.STRICT_PROTECTED;

import au.com.integradev.delphi.antlr.DelphiLexer;
import au.com.integradev.delphi.antlr.DelphiParser;
import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.antlr.runtime.Token;

public final class VisibilityNodeImpl extends AbstractDelphiNode implements VisibilityNode {
  public VisibilityNodeImpl(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public VisibilityType getVisibility() {
    switch (jjtGetId()) {
      case DelphiLexer.PUBLISHED:
        return PUBLISHED;
      case DelphiLexer.PUBLIC:
        return PUBLIC;
      case DelphiLexer.PROTECTED:
        return jjtGetChildId(0) == DelphiLexer.STRICT ? STRICT_PROTECTED : PROTECTED;
      case DelphiLexer.PRIVATE:
        return jjtGetChildId(0) == DelphiLexer.STRICT ? STRICT_PRIVATE : PRIVATE;
      default:
        throw new AssertionError(
            "Visibility node has unexpected token type: " + DelphiParser.tokenNames[jjtGetId()]);
    }
  }
}
