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
import au.com.integradev.delphi.type.intrinsic.IntrinsicType;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.TextLiteralNode;
import org.sonar.plugins.communitydelphi.api.type.Type;

public final class TextLiteralNodeImpl extends LiteralNodeImpl implements TextLiteralNode {
  private String image;

  public TextLiteralNodeImpl(Token token) {
    super(token);
  }

  public TextLiteralNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  @NotNull
  public Type getType() {
    IntrinsicType intrinsic =
        (getImageWithoutQuotes().length() == 1) ? IntrinsicType.CHAR : IntrinsicType.STRING;

    return getTypeFactory().getIntrinsic(intrinsic);
  }

  @Override
  public String getImage() {
    if (image == null) {
      StringBuilder imageBuilder = new StringBuilder("'");
      for (int i = 0; i < jjtGetNumChildren(); ++i) {
        DelphiNode child = jjtGetChild(i);
        switch (child.jjtGetId()) {
          case DelphiLexer.TkQuotedString:
            String withoutQuotes = getStringWithoutQuotes(child.getImage()).toString();
            String stringImage = withoutQuotes.replace("''", "'");
            imageBuilder.append(stringImage);
            break;

          case DelphiLexer.TkCharacterEscapeCode:
            String escapedChar = child.getImage();
            boolean isHex = escapedChar.startsWith("#$");
            escapedChar = escapedChar.substring(isHex ? 2 : 1);
            imageBuilder.append((char) Integer.parseInt(escapedChar, isHex ? 16 : 10));
            break;

          case DelphiLexer.TkEscapedCharacter:
            imageBuilder.append(child.getImage());
            break;

          default:
            // Do nothing
        }
      }
      imageBuilder.append("'");
      image = imageBuilder.toString();
    }
    return image;
  }

  @Override
  public CharSequence getImageWithoutQuotes() {
    return getStringWithoutQuotes(getImage());
  }

  private static CharSequence getStringWithoutQuotes(String string) {
    return string.subSequence(1, string.length() - 1);
  }
}
