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
package au.com.integradev.delphi.preprocessor.directive;

import static au.com.integradev.delphi.preprocessor.directive.expression.Expressions.nameReference;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import au.com.integradev.delphi.preprocessor.DelphiPreprocessor;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.directive.SwitchDirective.SwitchKind;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

class DirectivesTest {

  @Test
  void testIfSuccessfulBranch() {
    var trueBranch = new IfDirective(mock(DelphiToken.class), nameReference("True"));
    var falseBranch = new IfDirective(mock(DelphiToken.class), nameReference("False"));
    var unknownBranch = new IfDirective(mock(DelphiToken.class), nameReference("Unknown"));

    assertThat(trueBranch.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isTrue();
    assertThat(falseBranch.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isFalse();
    assertThat(unknownBranch.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isFalse();
  }

  @Test
  void testElseIfSuccessfulBranch() {
    var trueBranch = new ElseIfDirective(mock(DelphiToken.class), nameReference("True"));
    var falseBranch = new ElseIfDirective(mock(DelphiToken.class), nameReference("False"));
    var unknownBranch = new ElseIfDirective(mock(DelphiToken.class), nameReference("Unknown"));

    assertThat(trueBranch.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isTrue();
    assertThat(falseBranch.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isFalse();
    assertThat(unknownBranch.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isFalse();
  }

  @Test
  void testElseIsAlwaysSuccessfulBranch() {
    var elseBranch = new ElseDirective(mock(DelphiToken.class));
    assertThat(elseBranch.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isTrue();
  }

  @Test
  void testEndIfHasNoExecutionAction() {
    var elseBranch = new EndIfDirective(mock(DelphiToken.class));
    DelphiPreprocessor preprocessor = mock(DelphiPreprocessor.class);
    elseBranch.execute(preprocessor);
    verifyNoMoreInteractions(preprocessor);
  }

  @Test
  void testIfOptIsNeverSuccessfulBranchShouldFailOnUpgrade() {
    var directive = new IfOptDirective(mock(DelphiToken.class), SwitchKind.ALIGN, false);
    assertThat(directive.isSuccessfulBranch(mock(DelphiPreprocessor.class))).isFalse();
  }
}
