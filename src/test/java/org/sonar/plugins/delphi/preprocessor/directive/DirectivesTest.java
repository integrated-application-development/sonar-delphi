package org.sonar.plugins.delphi.preprocessor.directive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.sonar.plugins.delphi.preprocessor.directive.CompilerDirectiveType.ELSE;
import static org.sonar.plugins.delphi.preprocessor.directive.CompilerDirectiveType.ELSEIF;
import static org.sonar.plugins.delphi.preprocessor.directive.CompilerDirectiveType.ENDIF;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Expressions.nameReference;

import org.antlr.runtime.Token;
import org.junit.Test;
import org.sonar.plugins.delphi.preprocessor.DelphiPreprocessor;

public class DirectivesTest {

  @Test
  public void testIfSuccessfulBranch() {
    IfDirective trueBranch = new IfDirective(mock(Token.class), ELSEIF, nameReference("True"));

    IfDirective falseBranch = new IfDirective(mock(Token.class), ELSEIF, nameReference("False"));

    IfDirective unknownBranch =
        new IfDirective(mock(Token.class), ELSEIF, nameReference("Unknown"));

    assertThat(trueBranch.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isTrue();
    assertThat(falseBranch.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isFalse();
    assertThat(unknownBranch.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isFalse();
  }

  @Test
  public void testElseIfSuccessfulBranch() {
    ElseIfDirective trueBranch =
        new ElseIfDirective(mock(Token.class), ELSEIF, nameReference("True"));

    ElseIfDirective falseBranch =
        new ElseIfDirective(mock(Token.class), ELSEIF, nameReference("False"));

    ElseIfDirective unknownBranch =
        new ElseIfDirective(mock(Token.class), ELSEIF, nameReference("Unknown"));

    assertThat(trueBranch.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isTrue();
    assertThat(falseBranch.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isFalse();
    assertThat(unknownBranch.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isFalse();
  }

  @Test
  public void testElseIsAlwaysSuccessfulBranch() {
    ElseDirective elseBranch = new ElseDirective(mock(Token.class), ELSE);
    assertThat(elseBranch.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isTrue();
  }

  @Test
  public void testEndIfHasNoExecutionAction() {
    EndIfDirective elseBranch = new EndIfDirective(mock(Token.class), ENDIF);
    DelphiPreprocessor preprocessor = mock(DelphiPreprocessor.class);
    elseBranch.execute(preprocessor);
    verifyZeroInteractions(preprocessor);
  }

  @Test
  public void testIfOptIsNeverSuccessfulBranchShouldFailOnUpgrade() {
    IfOptDirective directive = new IfOptDirective(mock(Token.class), CompilerDirectiveType.IFOPT);
    assertThat(directive.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isFalse();
  }
}
