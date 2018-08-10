package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class ReRaiseExceptionRule extends DelphiRule {

    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx){

        if(node.getType() == DelphiLexer.EXCEPT){

        }

    }
}
