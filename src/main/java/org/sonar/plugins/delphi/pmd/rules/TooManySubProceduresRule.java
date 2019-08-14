package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

/**
 * This rule adds violations when there are too many defined sub-procedures in a top-level method,
 * regardless of sub-procedure nesting level
 */
public class TooManySubProceduresRule extends DelphiRule {
  private static final String VIOLATION_MESSAGE =
      "Code should not contain too many sub-procedures. Method has %d sub-procedures (Limit is %d)";

  @Override
  public void visit(DelphiNode node, RuleContext ctx) {
    if (shouldVisit(node)) {
      int count = countSubProcedures(node);
      int limit = getProperty(LIMIT);

      if (count > limit) {
        var violationNode = (DelphiNode) node.getFirstChildWithType(DelphiLexer.TkFunctionName);
        addViolation(ctx, violationNode, String.format(VIOLATION_MESSAGE, count, limit));
      }
    }
  }

  private int countSubProcedures(DelphiNode node) {
    int count = 0;

    DelphiNode blockDeclSection = node.nextNode();
    if (blockDeclSection != null) {
      for (int i = 0; i < blockDeclSection.getChildCount(); ++i) {
        Tree child = blockDeclSection.getChild(i);
        if (isMethodNode(child)) {
          count += countSubProcedures((DelphiNode) child) + 1;
        }
      }
    }

    return count;
  }

  private boolean shouldVisit(Tree node) {
    return isMethodNode(node) || node.getParent().getType() == DelphiLexer.IMPLEMENTATION;
  }

  private boolean isMethodNode(Tree node) {
    int type = node.getType();
    return type == DelphiLexer.CONSTRUCTOR
        || type == DelphiLexer.DESTRUCTOR
        || type == DelphiLexer.FUNCTION
        || type == DelphiLexer.PROCEDURE;
  }
}
