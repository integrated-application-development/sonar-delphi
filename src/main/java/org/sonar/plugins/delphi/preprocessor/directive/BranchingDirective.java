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
