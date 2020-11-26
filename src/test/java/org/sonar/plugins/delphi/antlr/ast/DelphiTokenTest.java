package org.sonar.plugins.delphi.antlr.ast;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.antlr.runtime.CommonToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.token.DelphiToken;

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
