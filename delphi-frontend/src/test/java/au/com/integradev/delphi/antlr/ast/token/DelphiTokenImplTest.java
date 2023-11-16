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
package au.com.integradev.delphi.antlr.ast.token;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.integradev.delphi.antlr.DelphiLexer;
import org.antlr.runtime.CommonToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DelphiTokenImplTest {
  private DelphiTokenImpl commentToken;
  private DelphiTokenImpl directiveToken;

  @BeforeEach
  void setup() {
    CommonToken antlrComment = new CommonToken(DelphiLexer.COMMENT);
    antlrComment.setText(
        "{ This is my multiline comment.\n As you can see, it's 3 whole lines.\n}");
    antlrComment.setLine(5);
    antlrComment.setCharPositionInLine(12);
    commentToken = new DelphiTokenImpl(antlrComment);

    CommonToken antlrDirective = new CommonToken(DelphiLexer.TkCompilerDirective);
    antlrDirective.setText(
        "{$I *.inc This is my multiline directive.\n As you can see, it's 3 whole lines.\n}");
    antlrDirective.setLine(5);
    antlrDirective.setCharPositionInLine(12);
    directiveToken = new DelphiTokenImpl(antlrDirective);
  }

  @Test
  void testGetBeginLine() {
    assertThat(commentToken.getBeginLine()).isEqualTo(directiveToken.getBeginLine()).isEqualTo(5);
  }

  @Test
  void testGetBeginColumn() {
    assertThat(commentToken.getBeginColumn())
        .isEqualTo(directiveToken.getBeginColumn())
        .isEqualTo(12);
  }

  @Test
  void testGetEndLine() {
    assertThat(commentToken.getEndLine()).isEqualTo(directiveToken.getEndLine()).isEqualTo(7);
  }

  @Test
  void testGetEndColumn() {
    assertThat(commentToken.getEndColumn()).isEqualTo(directiveToken.getEndColumn()).isEqualTo(1);
  }
}
