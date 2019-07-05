package org.sonar.plugins.delphi.pmd.rules;

import java.util.Collections;
import java.util.List;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public interface NameNodeFinderInterface {

  default DelphiPMDNode findNameNode(DelphiPMDNode node) {
    return null;
  }

  default List<DelphiPMDNode> findNameNodes(DelphiPMDNode node) {
    return Collections.emptyList();
  }
}
