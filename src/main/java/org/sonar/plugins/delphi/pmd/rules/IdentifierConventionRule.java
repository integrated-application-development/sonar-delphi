package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class IdentifierConventionRule extends DelphiRule {
    /**
     * This rule looks at all identifiers at ensures it follows the Delphi Case convention
     *
     *  @param node the current node
     *  @param ctx the ruleContext to store the violations
     */
    public void visit(DelphiPMDNode node, RuleContext ctx) {
        if (node.getType() == DelphiLexer.TkIdentifier) {
            char firstChar = node.getText().charAt(0);
            if (firstChar != Character.toUpperCase(firstChar)) {
                addViolation(ctx, node);
            }

        }
    }
}

