package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class TooManySubproceduresRule extends DelphiRule {

    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx) {
        System.out.print("WIP");
    }

}
