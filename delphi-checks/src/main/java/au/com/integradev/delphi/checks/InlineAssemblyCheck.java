package au.com.integradev.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.AsmStatementNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;

@Rule(key = "InlineAssembly")
public class InlineAssemblyCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this inline assembly code.";

  @Override
  public DelphiCheckContext visit(AsmStatementNode asm, DelphiCheckContext context) {
    context
        .newIssue()
        .onFilePosition(FilePosition.from(asm.getToken()))
        .withMessage(MESSAGE)
        .report();
    return super.visit(asm, context);
  }
}
