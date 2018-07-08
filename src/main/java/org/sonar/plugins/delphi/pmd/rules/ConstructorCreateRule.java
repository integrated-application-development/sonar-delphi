package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;


public class ConstructorCreateRule extends DelphiRule{

    private int CONSTRUCTOR_NAME_POS;

    @Override
    protected void init(){
        super.init();
        CONSTRUCTOR_NAME_POS = 1; // The first child position is the name of the constructor
    }

    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx){

        if(node.getType() == DelphiLexer.CONSTRUCTOR){



            System.out.print("DEBUG*************\n");

            Tree constructorName = node.getChild(CONSTRUCTOR_NAME_POS);
            System.out.print(constructorName + "\n");
            System.out.print(constructorName.getType() + "\n");

            String constructName = constructorName.getText();
            System.out.print(constructName + "\n");
            if (constructorName.getType() == DelphiLexer.TkFunctionArgs && constructorName != null){
                System.out.print(constructorName + "\n");
                System.out.print(constructorName.getType() + "\n");
                System.out.print(constructorName.getText() + "\n");

                if(!nameEndsWithCreate(constructName)) {
                    System.out.print("VIOLATION\n");
                    addViolation(ctx, node);
                }
            }

            System.out.print("END DEBUG************\n");
        }

    }

    private boolean nameEndsWithCreate(String constructName){
        String EXPECTED_CREATE_PREFIX = "Create";

        // Check the first 6 characters of the string for 'Create'
        String constructPrefex = constructName.substring(0, 5);

        return constructPrefex.equals(EXPECTED_CREATE_PREFIX);

    }
}
