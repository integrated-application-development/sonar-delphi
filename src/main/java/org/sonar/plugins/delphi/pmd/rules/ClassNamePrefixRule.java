package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class ClassNamePrefixRule extends DelphiRule {

    /**
     * Checks the name declarations used for the names of classes and enumerated class types, and raises a violation
     * if they do not begin with 'T'
     *
     * This is done by searching for nodes of the type 'TkNewType', and getting the next child of that node which
     * contains the name of the class or enumeration declaration. Within the context of the API this seems to be the
     * best way to find both class and enumerations (as there is no enumeration keyword in Delphi), but may give some
     * false positives.
     * @param node the current node
     * @param ctx the ruleContext to store the violations
     */
    @Override
    public  void visit(DelphiPMDNode node, RuleContext ctx){

        if (node.getType() == DelphiLexer.TkNewType){

            // The child node will contain the name of the declared class/enumeration
            Tree newTypeNode = node.getChild(0);

            String newTypeName = newTypeNode.getText();

            if (!newTypeName.startsWith("T")){
                addViolation(ctx, node);
            }
        }

    }
}
