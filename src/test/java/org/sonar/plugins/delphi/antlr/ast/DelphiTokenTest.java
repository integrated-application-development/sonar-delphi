package org.sonar.plugins.delphi.antlr.ast;

import static org.assertj.core.api.Assertions.assertThat;

import org.antlr.runtime.CommonToken;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.DelphiLexer;

public class DelphiTokenTest {
  private DelphiToken commentToken;

  @Before
  public void setup() {
    CommonToken commonToken = new CommonToken(DelphiLexer.COMMENT);
    commonToken.setText("{ This is my multiline comment.\n As you can see, it's 3 whole lines.\n}");
    commonToken.setLine(5);
    commonToken.setCharPositionInLine(12);
    commentToken = new DelphiToken(commonToken);
  }

  @Test
  public void testGetBeginLine() {
    assertThat(commentToken.getBeginLine()).isEqualTo(5);
  }

  @Test
  public void testGetBeginColumn() {
    assertThat(commentToken.getBeginColumn()).isEqualTo(12);
  }

  @Test
  public void testGetEndLine() {
    assertThat(commentToken.getEndLine()).isEqualTo(7);
  }

  @Test
  public void testGetEndColumn() {
    assertThat(commentToken.getEndColumn()).isEqualTo(1);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetNext() {
    commentToken.getNext();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetPreviousComment() {
    commentToken.getPreviousComment();
  }
}
