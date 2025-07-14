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
import au.com.integradev.delphi.preprocessor.TextBlockLineEndingMode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;
import org.antlr.runtime.Token;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.TextLiteralNode;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;

public final class TextLiteralNodeImpl extends DelphiNodeImpl implements TextLiteralNode {
  private String image;
  private String value;

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
  public Type getType() {
    IntrinsicType intrinsic =
        (getValue().length() == 1) ? IntrinsicType.CHAR : IntrinsicType.STRING;

    return getTypeFactory().getIntrinsic(intrinsic);
  }

  @Override
  public String getImage() {
    if (image == null) {
      image =
          getChildren().stream()
              .map(
                  child -> {
                    String result = child.getImage();
                    if (child.getTokenType() == DelphiTokenType.ESCAPED_CHARACTER) {
                      result = '^' + result;
                    }
                    return result;
                  })
              .collect(Collectors.joining());
    }
    return image;
  }

  @Override
  public String getValue() {
    if (value == null) {
      value = createValue();
    }
    return value;
  }

  @SuppressWarnings("removal")
  @Override
  public CharSequence getImageWithoutQuotes() {
    return getValue();
  }

  private String createValue() {
    if (isMultiline()) {
      return createMultilineValue();
    } else {
      return createSingleLineValue();
    }
  }

  private String createMultilineValue() {
    Deque<String> lines =
        getChild(0).getImage().lines().collect(Collectors.toCollection(ArrayDeque<String>::new));

    lines.removeFirst();

    String last = lines.removeLast();
    String indentation = readLeadingWhitespace(last);

    var registry = getAst().getDelphiFile().getTextBlockLineEndingModeRegistry();
    TextBlockLineEndingMode lineEndingMode = registry.getLineEndingMode(getTokenIndex());
    String lineEnding;

    switch (lineEndingMode) {
      case CR:
        lineEnding = "\r";
        break;
      case LF:
        lineEnding = "\n";
        break;
      default:
        lineEnding = "\r\n";
    }

    return lines.stream()
        .map(line -> Strings.CS.removeStart(line, indentation))
        .collect(Collectors.joining(lineEnding));
  }

  private static String readLeadingWhitespace(String input) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < input.length(); ++i) {
      char c = input.charAt(i);
      if (c <= 0x20 || c == 0x3000) {
        result.append(c);
      } else {
        break;
      }
    }
    return result.toString();
  }

  private String createSingleLineValue() {
    StringBuilder imageBuilder = new StringBuilder();

    for (DelphiNode child : getChildren()) {
      switch (child.getTokenType()) {
        case QUOTED_STRING:
          String stringImage = child.getImage();
          stringImage = stringImage.substring(1, stringImage.length() - 1);
          stringImage = stringImage.replace("''", "'");
          imageBuilder.append(stringImage);
          break;

        case CHARACTER_ESCAPE_CODE:
          imageBuilder.append(characterEscapeToChar(child.getImage()));
          break;

        case ESCAPED_CHARACTER:
          imageBuilder.append((char) ((child.getImage().charAt(0) + 64) % 128));
          break;

        default:
          // Do nothing
      }
    }

    return imageBuilder.toString();
  }

  private static char characterEscapeToChar(String image) {
    image = image.substring(1);
    int radix = 10;

    switch (image.charAt(0)) {
      case '$':
        radix = 16;
        image = image.substring(1);
        break;
      case '%':
        radix = 2;
        image = image.substring(1);
        break;
      default:
        // do nothing
    }

    image = StringUtils.remove(image, '_');

    return (char) Integer.parseInt(image, radix);
  }

  @Override
  public boolean isMultiline() {
    return getChild(0).getTokenType() == DelphiTokenType.MULTILINE_STRING;
  }
}
