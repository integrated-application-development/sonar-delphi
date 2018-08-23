package org.sonar.plugins.delphi.pmd;

import net.sourceforge.pmd.lang.ast.Node;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import java.io.File;
import java.util.List;

public class IdentifierCapitalisationTest {


    private static final String TEST_FILE = "/org/sonar/plugins/delphi/PMDTest/IdentifierCapitalisationTest.pas";

    @Test
    public void test(){
        File testFile = DelphiUtils.getResource(TEST_FILE);
        DelphiPMD pmd = new DelphiPMD();
        DelphiAST ast = new DelphiAST(testFile);
        List<Node> nodes = pmd.getNodesFromAST(ast);

        System.out.print("");
    }
}
