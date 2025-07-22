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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.antlr.DelphiLexer;
import au.com.integradev.delphi.antlr.ast.DelphiAstImpl;
import au.com.integradev.delphi.file.DelphiFile;
import au.com.integradev.delphi.preprocessor.CompilerSwitchRegistry;
import au.com.integradev.delphi.preprocessor.TextBlockLineEndingMode;
import au.com.integradev.delphi.preprocessor.TextBlockLineEndingModeRegistry;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import java.nio.charset.Charset;
import org.antlr.runtime.CommonToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.directive.SwitchDirective.SwitchKind;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;

class TextLiteralNodeImplTest {
  private boolean highCharUnicode;

  @BeforeEach
  void setup() {
    highCharUnicode = false;
  }

  @Test
  void testMultilineImage() {
    String image =
        "'''\n" //
            + "      Foo\n"
            + "      Bar\n"
            + "      Baz\n"
            + "      '''";

    TextBlockLineEndingModeRegistry registry = mock();
    when(registry.getLineEndingMode(anyInt())).thenReturn(TextBlockLineEndingMode.CRLF);

    DelphiFile delphiFile = mock();
    when(delphiFile.getTextBlockLineEndingModeRegistry()).thenReturn(registry);

    TextLiteralNodeImpl node = new TextLiteralNodeImpl(DelphiLexer.TkTextLiteral);
    node.setParent(DelphiAstImpl.create(delphiFile, mock()));
    node.addChild(commonNode(DelphiLexer.TkMultilineString, image));

    assertThat(node.getImage()).isEqualTo(image);
    assertThat(node.getValue())
        .isEqualTo(node.getImageWithoutQuotes())
        .isEqualTo("Foo\r\nBar\r\nBaz");
    assertThat(node.isMultiline()).isTrue();
  }

  @ParameterizedTest(name = "HIGHCHARUNICODE = {0}")
  @ValueSource(booleans = {true, false})
  void testGetImageWithCharacterEscapes(boolean highCharUnicode) {
    this.highCharUnicode = highCharUnicode;

    TextLiteralNodeImpl node = textNode();

    node.addChild(commonNode(DelphiLexer.TkQuotedString, "'F'"));
    node.addChild(commonNode(DelphiLexer.TkCharacterEscapeCode, "#111"));
    node.addChild(commonNode(DelphiLexer.TkCharacterEscapeCode, "#111"));
    node.addChild(commonNode(DelphiLexer.TkQuotedString, "'B'"));
    node.addChild(commonNode(DelphiLexer.TkCharacterEscapeCode, "#$61"));
    node.addChild(commonNode(DelphiLexer.TkCharacterEscapeCode, "#$72"));
    node.addChild(commonNode(DelphiLexer.TkQuotedString, "'B'"));
    node.addChild(commonNode(DelphiLexer.TkCharacterEscapeCode, "#$80"));
    node.addChild(commonNode(DelphiLexer.TkCharacterEscapeCode, "#$98"));
    node.addChild(commonNode(DelphiLexer.TkCharacterEscapeCode, "#$A3"));
    node.addChild(commonNode(DelphiLexer.TkCharacterEscapeCode, "#$20AC"));
    node.addChild(commonNode(DelphiLexer.TkQuotedString, "'az'"));

    assertThat(node.isMultiline()).isFalse();
    assertThat(node.getImage()).isEqualTo("'F'#111#111'B'#$61#$72'B'#$80#$98#$A3#$20AC'az'");
    if (highCharUnicode) {
      assertThat(node.getValue())
          .isEqualTo(node.getImageWithoutQuotes())
          .isEqualTo("FooBarB\u0080\u0098£€az");
      assertThat(node.getType().is(IntrinsicType.UNICODESTRING)).isTrue();
    } else {
      assertThat(node.getValue())
          .isEqualTo(node.getImageWithoutQuotes())
          .isEqualTo("FooBarB€˜£€az");
      assertThat(node.getType().is(IntrinsicType.ANSISTRING)).isTrue();
    }
  }

  @Test
  void testGetImageWithBinaryCharacterEscapes() {
    TextLiteralNodeImpl node = textNode();

    node.addChild(commonNode(DelphiLexer.TkQuotedString, "'F'"));
    node.addChild(commonNode(DelphiLexer.TkCharacterEscapeCode, "#%1101111"));
    node.addChild(commonNode(DelphiLexer.TkCharacterEscapeCode, "#%1101111"));

    assertThat(node.getImage()).isEqualTo("'F'#%1101111#%1101111");
    assertThat(node.getValue()).isEqualTo("Foo");
    assertThat(node.getType().is(IntrinsicType.UNICODESTRING)).isTrue();
  }

  @ParameterizedTest(name = "HIGHCHARUNICODE = {0}")
  @ValueSource(booleans = {true, false})
  void testSingleCharacterEscapeType(boolean highCharUnicode) {
    this.highCharUnicode = highCharUnicode;

    TextLiteralNodeImpl lowChar = textNode();
    lowChar.addChild(commonNode(DelphiLexer.TkCharacterEscapeCode, "#127"));

    TextLiteralNodeImpl highChar = textNode();
    highChar.addChild(commonNode(DelphiLexer.TkCharacterEscapeCode, "#128"));

    assertThat(lowChar.getType().is(IntrinsicType.WIDECHAR)).isTrue();

    if (highCharUnicode) {
      assertThat(highChar.getType().is(IntrinsicType.WIDECHAR)).isTrue();
    } else {
      assertThat(highChar.getType().is(IntrinsicType.ANSICHAR)).isTrue();
    }
  }

  @Test
  void testGetImageWithCaretNotation() {
    TextLiteralNodeImpl node = textNode();
    node.addChild(commonNode(DelphiLexer.TkQuotedString, "'F'"));
    node.addChild(commonNode(DelphiLexer.TkEscapedCharacter, "/"));
    node.addChild(commonNode(DelphiLexer.TkEscapedCharacter, "/"));
    node.addChild(commonNode(DelphiLexer.TkQuotedString, "'B'"));
    node.addChild(commonNode(DelphiLexer.TkEscapedCharacter, "!"));
    node.addChild(commonNode(DelphiLexer.TkEscapedCharacter, "2"));
    node.addChild(commonNode(DelphiLexer.TkQuotedString, "'B'"));
    node.addChild(commonNode(DelphiLexer.TkEscapedCharacter, "!"));
    node.addChild(commonNode(DelphiLexer.TkEscapedCharacter, ":"));

    assertThat(node.getImage()).isEqualTo("'F'^/^/'B'^!^2'B'^!^:");
    assertThat(node.getValue()).isEqualTo(node.getImageWithoutQuotes()).isEqualTo("FooBarBaz");
    assertThat(node.isMultiline()).isFalse();
  }

  private static DelphiNode commonNode(int tokenType, String image) {
    return new CommonDelphiNodeImpl(new CommonToken(tokenType, image));
  }

  private TextLiteralNodeImpl textNode() {
    var registry = mock(CompilerSwitchRegistry.class);
    when(registry.isActiveSwitch(eq(SwitchKind.HIGHCHARUNICODE), anyInt()))
        .thenAnswer(invocation -> highCharUnicode);

    var file = mock(DelphiFile.class);
    when(file.getCompilerSwitchRegistry()).thenReturn(registry);
    when(file.getTypeFactory()).thenReturn(TypeFactoryUtils.defaultFactory());
    when(file.getAnsiCharset()).thenReturn(Charset.forName("windows-1252"));

    var ast = mock(DelphiAstImpl.class);
    when(ast.getDelphiFile()).thenReturn(file);

    TextLiteralNodeImpl node = new TextLiteralNodeImpl(DelphiLexer.TkTextLiteral);
    node.setParent(ast);
    return node;
  }
}
