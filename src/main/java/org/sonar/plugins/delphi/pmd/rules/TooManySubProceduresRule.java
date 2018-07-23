package org.sonar.plugins.delphi.pmd.rules;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class TooManySubProceduresRule extends DelphiRule {

    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx) {

        // beginning of a procedure is identified by 'implementation' followed by 'procedure'
        if (node.toString().equals("implementation")){
            if (node.getChild(0 ) == null) { return; }
            if (!node.getChild(0).toString().equals("procedure") &&
                    !node.getChild(0).toString().equals("function")){
                return;
            }

            // subProcedureCount starts at -1, since the first counted procedure will be the outer procedure
            int subProcedureCount = -1;
            for (Object childTree : node.getChildren()){
                String child = childTree.toString();
                if (child.equals("procedure") || child.equals("function")){
                    subProcedureCount += 1;
                }
            }

            Integer threshold = getProperty(LIMIT);
            if (subProcedureCount > threshold){
                addViolation(ctx, node, "Code should not contain too many sub procedures or functions, " +
                        "limit of " + getProperty(LIMIT) + " exceeded.");
            }
        }
    }
}
