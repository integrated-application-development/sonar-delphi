package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class EnumNamePrefixRule extends DelphiRule {

    @Override
    public  void visit(DelphiPMDNode node, RuleContext ctx){

        if (node.getType() == DelphiLexer.TkNewType){



            for (int i = 0; i < node.getChildCount(); i++){
                Tree child = node.getChild(i);
               // System.out.print("NODE: " + child.getText() + "\n");
              //  System.out.print("TYPE: " + child.getType() + "\n");
               // System.out.print("NODE: " + node.getChild(i). + "\n");

            }

            //System.out.print("******************\n");
        }



    }
}
