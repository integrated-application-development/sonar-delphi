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
package org.sonar.plugins.delphi.preprocessor.directive;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.preprocessor.DelphiPreprocessor;

public class BranchingDirective extends AbstractCompilerDirective {
  private final Deque<BranchDirective> branches;

  BranchingDirective(BranchDirective directive) {
    super(directive.getToken(), directive.getType());
    branches = new ArrayDeque<>();
    addBranch(directive);
  }

  public void addBranch(BranchDirective branch) {
    branches.add(branch);
  }

  public void addDirective(CompilerDirective directive) {
    branches.getLast().addDirective(directive);
  }

  public void addToken(Token token) {
    branches.getLast().addToken(token);
  }

  private Deque<BranchDirective> getBranches() {
    return branches;
  }

  @Override
  public void execute(DelphiPreprocessor preprocessor) {
    boolean foundBranch = false;
    for (BranchDirective branch : branches) {
      if (foundBranch || !branch.isSuccessfulBranch(preprocessor)) {
        deleteBranch(branch, preprocessor);
        continue;
      }

      branch.execute(preprocessor);
      foundBranch = true;
    }
  }

  private static void deleteBranch(BranchDirective branch, DelphiPreprocessor preprocessor) {
    for (Token token : branch.getTokens()) {
      preprocessor.deleteToken(token);
    }

    List<BranchingDirective> nestedDirectives =
        branch.getDirectives().stream()
            .filter(BranchingDirective.class::isInstance)
            .map(BranchingDirective.class::cast)
            .collect(Collectors.toList());

    for (BranchingDirective directive : nestedDirectives) {
      directive.getBranches().forEach(innerBranch -> deleteBranch(innerBranch, preprocessor));
    }
  }
}
