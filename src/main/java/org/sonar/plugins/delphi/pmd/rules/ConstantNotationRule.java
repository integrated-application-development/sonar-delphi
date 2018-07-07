package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.tree.Tree;
import org.javacc.jjtree.ASTBNFAssignment;
import org.javacc.jjtree.ASTNodeDescriptor;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

import java.util.List;
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

            System.out.print("**********DEBUG**************\n");
            System.out.print("IN THE RULE CONSTANTS\n");
            System.out.print(node.getBeginLine()+ "\n");
            System.out.print(node.getEndLine()+ "\n");
            node.getChildType(3);

            //System.out.print(node.findChildrenOfType(node.findDescendantsOfType(node.getType()));
            System.out.print("NODE TYPE:" + node.getType() + "\n");
            System.out.print("CHILD TYPE:" + node.getChildType(3) + "\n");

            System.out.print("NODE:" + node + "\n");

            for(int i = 0; i < node.getChildCount(); i++){

                Tree childNode = node.getChild(i);
                if (childNode != null) {
                    System.out.print("CHILD NODE:" + childNode + "\n");
                    System.out.print("CHILD TYPE:" + childNode.getType() +"\n");

                    Tree assignmentNode = null;
                    try {
                        assignmentNode = node.getChild(i + 1);
                    }catch (IndexOutOfBoundsException e){
                        continue;
                    }

                    if (assignmentNode.getType() == 50) {
                        String constName = childNode.getText();

                        if (!nameStartsWithC(constName)){
                            addViolation(ctx, (Node) childNode);
                            System.out.print("FOUND VIOLATION\n");
                        }

                    }


                }
            }

            //System.out.print("CHILD NODES:" + node.findChildrenOfType(AST) + "\n");


            System.out.print("**********END DEBUG**************\n");
        }
    }


    private boolean nameStartsWithC(String constName){

        String constPrefix = constName.substring(0, 2);

        if(!constPrefix.equals("C_")){
            return true;
        }
        return false;
    }


}
