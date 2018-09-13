package org.sonar.plugins.delphi.pmd;

import java.io.File;
import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class EnumNameTest {


  private static final String TEST_FILE = "/org/sonar/plugins/delphi/PMDTest/EnumTest.pas";

  // TODO make this testDefinitionsIncludes actually testDefinitionsIncludes, was just being used for debugging
  @Test
  public void test() {
    File testFile = DelphiUtils.getResource(TEST_FILE);
    DelphiPMD pmd = new DelphiPMD();
    DelphiAST ast = new DelphiAST(testFile);
    List<Node> nodes = pmd.getNodesFromAST(ast);

    System.out.print("");
  }
}
