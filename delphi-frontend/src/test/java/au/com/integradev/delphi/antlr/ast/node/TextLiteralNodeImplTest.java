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

import static org.assertj.core.api.Assertions.assertThat;

import au.com.integradev.delphi.antlr.DelphiLexer;
import org.antlr.runtime.CommonToken;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

class TextLiteralNodeImplTest {
  @Test
  void testMultilineImage() {
    String image =
        "'''\n" //
            + "      Foo\n"
            + "      Bar\n"
            + "      Baz\n"
            + "      '''";

    TextLiteralNodeImpl node = new TextLiteralNodeImpl(DelphiLexer.TkTextLiteral);
    node.addChild(createNode(DelphiLexer.TkMultilineString, image));

    assertThat(node.getImage()).isEqualTo(image);
    assertThat(node.getValue()).isEqualTo(node.getImageWithoutQuotes()).isEqualTo("Foo\nBar\nBaz");
    assertThat(node.isMultiline()).isTrue();
  }

  @Test
  void testGetImageWithCharacterEscapes() {
    TextLiteralNodeImpl node = new TextLiteralNodeImpl(DelphiLexer.TkTextLiteral);
    node.addChild(createNode(DelphiLexer.TkQuotedString, "'F'"));
    node.addChild(createNode(DelphiLexer.TkCharacterEscapeCode, "#111"));
    node.addChild(createNode(DelphiLexer.TkCharacterEscapeCode, "#111"));
    node.addChild(createNode(DelphiLexer.TkQuotedString, "'B'"));
    node.addChild(createNode(DelphiLexer.TkCharacterEscapeCode, "#$61"));
    node.addChild(createNode(DelphiLexer.TkCharacterEscapeCode, "#$72"));
    node.addChild(createNode(DelphiLexer.TkQuotedString, "'B'"));
    node.addChild(createNode(DelphiLexer.TkCharacterEscapeCode, "#%01100001"));
    node.addChild(createNode(DelphiLexer.TkCharacterEscapeCode, "#%01111010"));

    assertThat(node.getImage()).isEqualTo("'F'#111#111'B'#$61#$72'B'#%01100001#%01111010");
    assertThat(node.getValue()).isEqualTo(node.getImageWithoutQuotes()).isEqualTo("FooBarBaz");
    assertThat(node.isMultiline()).isFalse();
  }

  @Test
  void testGetImageWithCaretNotation() {
    TextLiteralNodeImpl node = new TextLiteralNodeImpl(DelphiLexer.TkTextLiteral);
    node.addChild(createNode(DelphiLexer.TkQuotedString, "'F'"));
    node.addChild(createNode(DelphiLexer.TkEscapedCharacter, "/"));
    node.addChild(createNode(DelphiLexer.TkEscapedCharacter, "/"));
    node.addChild(createNode(DelphiLexer.TkQuotedString, "'B'"));
    node.addChild(createNode(DelphiLexer.TkEscapedCharacter, "!"));
    node.addChild(createNode(DelphiLexer.TkEscapedCharacter, "2"));
    node.addChild(createNode(DelphiLexer.TkQuotedString, "'B'"));
    node.addChild(createNode(DelphiLexer.TkEscapedCharacter, "!"));
    node.addChild(createNode(DelphiLexer.TkEscapedCharacter, ":"));

    assertThat(node.getImage()).isEqualTo("'F'^/^/'B'^!^2'B'^!^:");
    assertThat(node.getValue()).isEqualTo(node.getImageWithoutQuotes()).isEqualTo("FooBarBaz");
    assertThat(node.isMultiline()).isFalse();
  }

  private static DelphiNode createNode(int tokenType, String image) {
    return new CommonDelphiNodeImpl(new CommonToken(tokenType, image));
  }
}
