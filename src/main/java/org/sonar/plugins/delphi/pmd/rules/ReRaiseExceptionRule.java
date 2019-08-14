package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

/**
 * This rule looks for exception blocks where the caught exception is explicitly re-raised. This is
 * a Bad Thing™ because the exception will be freed at the end of the handler, causing tricky access
 * errors
 *
 * @see <a
 *     href="zerolith.com/delphi/on-delphi-exception-raising-re-raising-and-try-except-blocks.html">
 *     On Delphi Exception raising, re-raising and try-except blocks</a>
 * @see <a href="http://delphi.org/2017/06/really-bad-exception-abuse/">Exceptionally Bad Exception
 *     Abuse</a>
 */
public class ReRaiseExceptionRule extends DelphiRule {

  @Override
  public void visit(DelphiNode node, RuleContext ctx) {
    if (node.getType() != DelphiLexer.TkExceptionHandlerIdent) {
      return;
    }

    DelphiNode handler = node.findNextSiblingOfType(DelphiLexer.TkExceptionHandler);
    String exceptionHandlerIdent = node.getChild(0).getText();
    List<Tree> raiseNodes = handler.findAllChildren(DelphiLexer.RAISE);

    for (Tree raiseNode : raiseNodes) {
      Tree parent = raiseNode.getParent();
      int childIndex = raiseNode.getChildIndex();

      if (childIndex + 1 == parent.getChildCount()) {
        continue;
      }

      Tree nextNode = parent.getChild(childIndex + 1);

      if (nextNode.getText().equalsIgnoreCase(exceptionHandlerIdent)) {
        addViolation(ctx, (DelphiNode) nextNode);
      }
    }
  }
}
