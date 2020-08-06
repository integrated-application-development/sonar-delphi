package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.LIMIT;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.MethodBodyNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.CognitiveComplexityVisitor;
import org.sonar.plugins.delphi.antlr.ast.visitors.CognitiveComplexityVisitor.Data;

public class CognitiveComplexityRule extends AbstractDelphiRule {
  private static final CognitiveComplexityVisitor COGNITIVE_VISITOR =
      new CognitiveComplexityVisitor() {
        @Override
        public Data visit(MethodBodyNode body, Data data) {
          // Skip the block declaration section so we don't count sub-procedures.
          return body.getBlock().accept(this, data);
        }
      };

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    Data result = COGNITIVE_VISITOR.visit(method, new Data());
    int complexity = result.getComplexity();
    int limit = getProperty(LIMIT);

    if (complexity > limit) {
      addViolationWithMessage(
          data,
          method.getMethodNameNode(),
          "The Cognitive Complexity of this method \"{0}\""
              + " is {1} which is greater than {2} authorized.",
          new Object[] {method.simpleName(), complexity, limit});
    }
    return super.visit(method, data);
  }
}
