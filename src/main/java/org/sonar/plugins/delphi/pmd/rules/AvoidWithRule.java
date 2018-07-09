package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.pmd.DelphiPMD;

public class NoWithKeywordRule extends DelphiRule {
    private String sonarMessage;

    protected void init(){
        super.init();
        sonarMessage = "Delphi code must not contain 'with' keyword.";
    }

    public void visit(DelphiPMDNode node, RuleContext ctx){
        if (node.getType() == DelphiLexer.WITH){
            addViolation(ctx, node);
        }
    }

}
