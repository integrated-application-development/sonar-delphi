package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

import java.util.List;

public class IdentifierConventionRule extends DelphiRule {

    /**
     * Confirms particular variable identifiers (TkVariableIdents) use the expected convention,
     * the first letter should be an uppercase
     * @param node the current node
     * @param ctx the ruleContext to store the violations
     */
    @Override
    public void visit(DelphiPMDNode node , RuleContext ctx){
        if(node.getType() == DelphiLexer.TkVariableIdents){
            List<Tree> children = node.findAllChildren(DelphiLexer.TkVariableIdents);
            for(Tree c : children){
                String name = c.getText();
                char fChar = name.charAt(0);

                if (fChar != Character.toUpperCase(fChar)){
                    addViolation(ctx, (DelphiPMDNode) c);
                }

            }
        }
    }
}
