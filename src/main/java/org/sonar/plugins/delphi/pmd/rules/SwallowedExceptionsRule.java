package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

/**
 * Any case where an except block or an exception handler is empty means that any raised exception
 * is silently swallowed.
 */
public class SwallowedExceptionsRule extends DelphiRule {

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    if (node.getType() == DelphiLexer.EXCEPT && isEmptyExceptBlock(node)) {
      addViolation(ctx, node);
    }

    if (node.getType() == DelphiLexer.TkExceptionHandler && isEmptyExceptionHandler(node)) {
      addViolation(ctx, node.prevNode().prevNode());
    }
  }

  private boolean isEmptyExceptBlock(DelphiPMDNode node) {
    DelphiPMDNode nextNode = node.nextNode();

    return nextNode != null && nextNode.getType() == DelphiLexer.END;
  }

  private boolean isEmptyExceptionHandler(DelphiPMDNode node) {
    if (node.getChildCount() == 0) {
      return true;
    }

    Tree statementNode = node.getChild(0);
    return statementNode.getType() == DelphiLexer.BEGIN && statementNode.getChildCount() == 1;
  }
}
