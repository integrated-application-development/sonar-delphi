package org.sonar.plugins.delphi.pmd;

import net.sourceforge.pmd.lang.ast.Node;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import java.io.File;
import java.util.List;

public class IdentifierConventionTest {
    private static final String TEST = "/org/sonar/plugins/delphi/PMDTest/IdentifierConvention.pas";

    @Test
    public void test(){
        File File = DelphiUtils.getResource(TEST);
        DelphiPMD pmd = new DelphiPMD();
        DelphiAST ast = new DelphiAST(File);
        List<Node> nodes = pmd.getNodesFromAST(ast);


        System.out.print("");
    }
}
