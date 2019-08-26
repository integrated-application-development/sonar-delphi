package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.LIMIT;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.MethodBodyNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.CyclomaticComplexityVisitor;
import org.sonar.plugins.delphi.antlr.ast.visitors.CyclomaticComplexityVisitor.Data;

public class CyclomaticComplexityRule extends AbstractDelphiRule {
  private static final String VIOLATION_MESSAGE =
      "The Cyclomatic Complexity of this method \"%s\" is %d which is greater than %d authorized.";

  private static final CyclomaticComplexityVisitor CYCLOMATIC_VISITOR =
      new CyclomaticComplexityVisitor() {
        @Override
        public Data visit(MethodBodyNode body, Data data) {
          // Skip the block declaration section so we don't count sub-procedures.
          return body.getBlock().accept(this, data);
        }
      };

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    Data result = CYCLOMATIC_VISITOR.visit(method, new Data());
    int complexity = result.getComplexity();
    int limit = getProperty(LIMIT);

    if (complexity > limit) {
      String message = String.format(VIOLATION_MESSAGE, method.getSimpleName(), complexity, limit);
      addViolationWithMessage(data, method.getMethodHeading().getMethodName(), message);
    }
    return super.visit(method, data);
  }
}
