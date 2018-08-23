package org.sonar.plugins.delphi.pmd.rules;

import net.sf.saxon.exslt.Common;
import net.sourceforge.pmd.RuleContext;

import org.antlr.runtime.tree.CommonTree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

import java.util.HashMap;
import java.util.HashSet;

public class IdentifierCapitalisationRule extends DelphiRule {

    /**
     *
     * @param node the current node
     * @param ctx the ruleContext to store the violations
     */
    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx) {
        HashMap<String,CommonTree> knownIdentifiers = new HashMap<String,CommonTree>();
        //change to hashmap <key: node.text, Value: commontree node object>

        if(node.getType() == DelphiLexer.TkIdentifier){//The node is an identifier
            CommonTree identifierNode = node;

            //check if we've visited this identifier before
            System.out.print("******************************* " + knownIdentifiers.containsKey(identifierNode.getText().toLowerCase()));
            if(knownIdentifiers.containsKey(identifierNode.getText().toLowerCase())){ //Yes.
               if(!(knownIdentifiers.get(identifierNode.getText().toLowerCase()).getText().equals(identifierNode.getText()))){//compare existing identifier text to this identifiers text and if they differ raise violation on current identifier
                    addViolation(ctx, (DelphiPMDNode) identifierNode);
                }

            }else{//No.
                knownIdentifiers.put(identifierNode.getText().toLowerCase(),identifierNode);
                System.out.print("******************************************************" + knownIdentifiers);

            }



        }



    }



}
