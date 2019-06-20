package org.sonar.plugins.delphi.pmd;

import java.io.File;
import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import org.junit.Ignore;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class IdentifierConventionTest {

  private static final String TEST = "/org/sonar/plugins/delphi/PMDTest/IdentifierConvention.pas";

  @Ignore // As far as I can tell, the file is ill-formed and the test doesn't even do anything...
  @Test
  public void test() {
    File File = DelphiUtils.getResource(TEST);
    DelphiPMD pmd = new DelphiPMD();
    DelphiAST ast = new DelphiAST(File);
    List<Node> nodes = pmd.getNodesFromAST(ast);

    System.out.print("");
  }
}
