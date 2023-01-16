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
package org.sonar.plugins.communitydelphi.preprocessor.directive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.sonar.plugins.communitydelphi.preprocessor.directive.CompilerDirectiveType.ELSE;
import static org.sonar.plugins.communitydelphi.preprocessor.directive.CompilerDirectiveType.ELSEIF;
import static org.sonar.plugins.communitydelphi.preprocessor.directive.CompilerDirectiveType.ENDIF;
import static org.sonar.plugins.communitydelphi.preprocessor.directive.expression.Expressions.nameReference;

import org.antlr.runtime.Token;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.preprocessor.DelphiPreprocessor;

class DirectivesTest {

  @Test
  void testIfSuccessfulBranch() {
    IfDirective trueBranch = new IfDirective(mock(Token.class), ELSEIF, nameReference("True"));

    IfDirective falseBranch = new IfDirective(mock(Token.class), ELSEIF, nameReference("False"));

    IfDirective unknownBranch =
        new IfDirective(mock(Token.class), ELSEIF, nameReference("Unknown"));

    assertThat(trueBranch.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isTrue();
    assertThat(falseBranch.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isFalse();
    assertThat(unknownBranch.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isFalse();
  }

  @Test
  void testElseIfSuccessfulBranch() {
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
  void testElseIsAlwaysSuccessfulBranch() {
    ElseDirective elseBranch = new ElseDirective(mock(Token.class), ELSE);
    assertThat(elseBranch.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isTrue();
  }

  @Test
  void testEndIfHasNoExecutionAction() {
    EndIfDirective elseBranch = new EndIfDirective(mock(Token.class), ENDIF);
    DelphiPreprocessor preprocessor = mock(DelphiPreprocessor.class);
    elseBranch.execute(preprocessor);
    verifyNoMoreInteractions(preprocessor);
  }

  @Test
  void testIfOptIsNeverSuccessfulBranchShouldFailOnUpgrade() {
    IfOptDirective directive = new IfOptDirective(mock(Token.class), CompilerDirectiveType.IFOPT);
    assertThat(directive.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isFalse();
  }
}
