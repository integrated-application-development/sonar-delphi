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
package au.com.integradev.delphi.antlr.ast;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import au.com.integradev.delphi.antlr.DelphiLexer;
import au.com.integradev.delphi.antlr.ast.token.DelphiToken;
import org.antlr.runtime.CommonToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DelphiTokenTest {
  private DelphiToken commentToken;

  @BeforeEach
  void setup() {
    CommonToken commonToken = new CommonToken(DelphiLexer.COMMENT);
    commonToken.setText("{ This is my multiline comment.\n As you can see, it's 3 whole lines.\n}");
    commonToken.setLine(5);
    commonToken.setCharPositionInLine(12);
    commentToken = new DelphiToken(commonToken);
  }

  @Test
  void testGetBeginLine() {
    assertThat(commentToken.getBeginLine()).isEqualTo(5);
  }

  @Test
  void testGetBeginColumn() {
    assertThat(commentToken.getBeginColumn()).isEqualTo(12);
  }

  @Test
  void testGetEndLine() {
    assertThat(commentToken.getEndLine()).isEqualTo(7);
  }

  @Test
  void testGetEndColumn() {
    assertThat(commentToken.getEndColumn()).isEqualTo(1);
  }

  @Test
  void testGetNext() {
    assertThatThrownBy(() -> commentToken.getNext())
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void testGetPreviousComment() {
    assertThatThrownBy(() -> commentToken.getPreviousComment())
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
