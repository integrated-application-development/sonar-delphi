package org.sonar.plugins.delphi.pmd.rules;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.ExceptItemNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.IdentifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.RaiseStatementNode;

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
public class ReRaiseExceptionRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(ExceptItemNode handler, RuleContext data) {
    for (IdentifierNode raise : findViolations(handler)) {
      addViolation(data, raise);
    }
    return super.visit(handler, data);
  }

  private List<IdentifierNode> findViolations(ExceptItemNode handler) {
    IdentifierNode identifier = handler.getExceptionIdentifier();
    if (identifier != null) {
      return handler.findDescendantsOfType(RaiseStatementNode.class).stream()
          .map(RaiseStatementNode::getRaiseExpression)
          .filter(Objects::nonNull)
          .map(ExpressionNode::skipParentheses)
          .filter(PrimaryExpressionNode.class::isInstance)
          .map(expr -> expr.jjtGetChild(0))
          .filter(IdentifierNode.class::isInstance)
          .map(IdentifierNode.class::cast)
          .filter(raised -> raised.hasImageEqualTo(identifier.getImage()))
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }
}
