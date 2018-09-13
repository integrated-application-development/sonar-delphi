package org.sonar.plugins.delphi.pmd;

import java.io.File;
import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import org.junit.Ignore;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class ConstructorCreateTest {

  private static final String TEST_FILE = "/org/sonar/plugins/delphi/PMDTest/ReRaiseExceptionTest.pas";

  // TODO make this testDefinitionsIncludes actually testDefinitionsIncludes, was just being used for debugging
  @Ignore
  @Test
  public void test() {
    File testFile = DelphiUtils.getResource(TEST_FILE);
    DelphiPMD pmd = new DelphiPMD();
    DelphiAST ast = new DelphiAST(testFile);
    List<Node> nodes = pmd.getNodesFromAST(ast);
  }
}
