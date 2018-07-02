package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

import java.util.regex.Pattern;

public class ConstantNotationRule extends DelphiRule {

    private String sonarMessage;

    @Override
    protected void init(){
        super.init();
        sonarMessage = "Constant values should be prepended with C_";

    }

    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx){

        if(node.getType() == DelphiLexer.CONST){

            for(int i = 0; i < node.getChildCount(); i++){
                // fixme getChildCount gets every single token under a const declaration, so get 25 violations
                String constName =  node.getChild(i).getText();


                if(!constName.startsWith("C_")){
                    addViolation(ctx, node);
                }
            }

        }
    }


}
