package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;


public class ConstructorCreateRule extends DelphiRule{


    @Override
    protected void init(){
        super.init();
    }

    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx){

        if(node.getType() == DelphiLexer.CONSTRUCTOR){

            addViolation(ctx, node);

            System.out.print("DEBUG*************\n");

            for(int i = 0; i < node.getChildCount(); i++){
                Tree constructorDeclaration = node.getChild(i);
                System.out.print(constructorDeclaration + "\n");
                System.out.print(constructorDeclaration.getType() + "\n");
                System.out.print(constructorDeclaration.getText() + "\n");


            }

            //addViolation(ctx, node);

           // Tree constructorDeclaration = node.getChild(1);
            //System.out.print(constructorDeclaration + "\n");
           // System.out.print(constructorDeclaration.getType() + "\n");
           //System.out.print(constructorDeclaration.getText() + "\n");

            System.out.print("END DEBUG************\n");
        }

    }
}
