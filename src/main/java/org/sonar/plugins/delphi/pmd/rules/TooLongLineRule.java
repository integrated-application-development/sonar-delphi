package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for counting line characters. If too long, creates a violation.
 */
public class TooLongLineRule extends DelphiRule{
    private int lineNumber;
    private int lineLength;
    private int limit;
    private ArrayList checkedLines = new ArrayList<Integer>();

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
        Tree astTree = node.getASTTree();
        String line = ((ASTTree) astTree).getFileSourceLine(node.getLine());


        if(line.length() > limit && !checkedLines.contains(nodeLineNumber)){
            checkedLines.add(nodeLineNumber);
            addViolation(ctx, node);
        }



        if(nodeLineNumber != lineNumber){
            lineLength = 0;
            lineNumber = nodeLineNumber;
            lineLength -= 1;
        }

        lineLength += nodeTextLength + 1;

        if(lineLength > limit){
            String sonarMessage = "Line too long (" + lineLength + " characters). Maximum character count should be "
                    + limit + ".";
            addViolation(ctx, node, sonarMessage);
        }

        System.out.print("***********\n");
        System.out.print("Node: " + node.getText() + "\n");
        System.out.print("Node line number " + nodeLineNumber + "\n");
//        System.out.print("NodeLength: " + nodeTextLength + "\n");
//        System.out.print("LineLength: " + lineLength);
        System.out.print(line);
        System.out.print("\n***********\n");
        System.out.print("");
    }

}
