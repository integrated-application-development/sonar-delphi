package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Class for counting line characters. If too long, creates a violation.
 */
public class TooLongLineRule extends DelphiRule{
    private int lineNumber;
    private int lineLength;
    private int limit;

    @Override
    protected void init(){
        super.init();
        lineNumber = 0;
        lineLength = 0;
        limit = 100;


    }

    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx) {
        int nodeTextLength = node.getText().length();
        int nodeLineNumber = node.getLine();

        if(nodeLineNumber != lineNumber){
            lineLength = 0;
            lineNumber = nodeLineNumber;
        }

        lineLength += nodeTextLength;

        if(lineLength > limit){
            String sonarMessage = "Line " + nodeLineNumber + " is too long (" + lineLength + " characters). Maximum character count should be "
                    + limit + ".";
            addViolation(ctx, node, sonarMessage);
        }

        System.out.print("***********\n");
        System.out.print("Node: " + node.getText() + "\n");
        System.out.print("NodeLength: " + nodeTextLength + "\n");
        System.out.print("LineLength: " + lineLength);
        System.out.print("\n***********\n");
    }

}
