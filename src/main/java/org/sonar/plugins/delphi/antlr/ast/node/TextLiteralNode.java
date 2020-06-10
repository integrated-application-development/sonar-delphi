package org.sonar.plugins.delphi.antlr.ast.node;

import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.CHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.UNICODESTRING;

import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;

public final class TextLiteralNode extends LiteralNode {
  private String image;

  public TextLiteralNode(Token token) {
    super(token);
  }

  public TextLiteralNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  @NotNull
  public Type getType() {
    if (getImageWithoutQuotes().length() == 1) {
      return CHAR.type;
    }

    return UNICODESTRING.type;
  }

  @Override
  public String getImage() {
    if (image == null) {
      StringBuilder imageBuilder = new StringBuilder("'");
      for (int i = 0; i < jjtGetNumChildren(); ++i) {
        Node child = jjtGetChild(i);
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
            imageBuilder.append((char) parseImage(escapedChar, isHex ? 16 : 10));
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

  public CharSequence getImageWithoutQuotes() {
    return getStringWithoutQuotes(getImage());
  }

  private static CharSequence getStringWithoutQuotes(String string) {
    return string.subSequence(1, string.length() - 1);
  }
}
