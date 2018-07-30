package org.sonar.plugins.delphi.pmd.rules;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

import java.util.List;

public class TooManySubProceduresRule extends DelphiRule {

    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx) {

        Integer threshold = getProperty(LIMIT);

        if (node.toString().equals("implementation")){
            List kids = node.getChildren();
            int subProcedureCounter = 0;

            // subProcedureDepth tracks whether we are in a procedure, sub procedure, or sub-sub procedures.
            // -1 means not in a procedure
            // 0 means in an out procedure
            // 1 means in a sub procedure
            // n means in a nth level sub procedure
            // we treat any nth level sub procedure the same as any sub procedures
            int subProcedureDepth = -1;

            for (int i=0; i<=kids.size(); i++){
                if((kids.get(i).toString().equals("procedure")) || (kids.get(i).toString().equals("function"))){
                    subProcedureDepth += 1;
                    if(subProcedureDepth > 0){
                        subProcedureCounter += 1;
                    }
                    if (subProcedureCounter > threshold){
                        addViolation(ctx, node, "Code should not contain too many sub procedures or functions, " +
                        "limit of " + getProperty(LIMIT) + " exceeded.");
                    }
                }
                else if(kids.get(i).toString().equals("begin")){
                    subProcedureDepth -= 1;
                }
            }
        }
    }
}
