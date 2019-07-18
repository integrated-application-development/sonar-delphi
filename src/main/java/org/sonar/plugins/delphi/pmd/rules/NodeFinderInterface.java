package org.sonar.plugins.delphi.pmd.rules;

import java.util.Collections;
import java.util.List;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public interface NodeFinderInterface {

  default DelphiPMDNode findNode(DelphiPMDNode node) {
    return null;
  }

  default List<DelphiPMDNode> findNodes(DelphiPMDNode node) {
    return Collections.emptyList();
  }
}
