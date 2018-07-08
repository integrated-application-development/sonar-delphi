package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class ConstantNotationRule extends DelphiRule {

    private String sonarMessage;

    @Override
    protected void init(){
        super.init();
        sonarMessage = "Constant values should be prepended with C_";

    }

    /**
     * This rule will find a 'const' block, and search through it's child nodes for assignments made to constant values.
     * When one is found, the node before it is considered the name of the declaraed constant. This is then checked for
     * correct notation and if not beginning with C_, a violation is raised in SonarQube.
     * @param node the current node
     * @param ctx the ruleContext to store the violations
     */
    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx){

        if(node.getType() == DelphiLexer.CONST){

            // For every child in a const block, check if any are equals nodes (type 50)
            for(int i = 0; i < node.getChildCount() - 1; i++){

                Tree childNode = node.getChild(i);
                if (childNode != null) {

                    int childType = childNode.getType();

                    if(childType == DelphiLexer.EQUAL){
                        // Get the node before the equals node, as that will be the name used to define it
                        Tree assignmentNode = node.getChild(i - 1);

                        String constName = assignmentNode.getText();

                        if (!nameStartsWithC_(constName)){

                            addViolation(ctx, (DelphiPMDNode) assignmentNode);

                        }
                    }

                }
            }

        }
    }


    /**
     * Check the first two characters of the string used to define the constant, return false if it does not begin
     * with C_
     * @param constName The name of the constant value assigned
     * @return True if starts with C_, false if not
     */
    private boolean nameStartsWithC_(String constName){

        String EXPECTED_CONST_PREFIX = "C_";

        // Get the substring of the first two characters and check if the value start with the correct characters
        String constPrefix = constName.substring(0, 2); // TODO should this be one?

        return constPrefix.equals(EXPECTED_CONST_PREFIX);
    }


}
