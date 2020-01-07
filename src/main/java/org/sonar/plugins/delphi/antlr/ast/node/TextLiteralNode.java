package org.sonar.plugins.delphi.antlr.ast.node;

import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.CHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.STRING;

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
    if (getValueAsString().length() == 1) {
      return CHAR.type;
    }

    return STRING.type;
  }

  @Override
  public String getImage() {
    if (image == null) {
      StringBuilder imageBuilder = new StringBuilder();
      for (int i = 0; i < jjtGetNumChildren(); ++i) {
        Node child = jjtGetChild(i);
        switch (child.jjtGetId()) {
          case DelphiLexer.TkQuotedString:
            imageBuilder.append(processString(child.getImage()));
            break;

          case DelphiLexer.TkCharacterEscapeCode:
            String escapeImage = child.getImage();
            boolean isHex = escapeImage.startsWith("#$");
            escapeImage = escapeImage.substring(isHex ? 2 : 1);
            imageBuilder.append((char) parseImage(escapeImage, isHex ? 16 : 10));
            break;

          case DelphiLexer.TkEscapedCharacter:
            imageBuilder.append(child.getImage());
            break;

          default:
            // Do nothing
        }
      }
      image = imageBuilder.toString();
    }
    return image;
  }

  private static String processString(String image) {
    if (image.length() > 2) {
      image = image.replace("''", "'");
    }
    return image.substring(1, image.length() - 1);
  }
}
