package org.sonar.plugins.delphi.pmd.rules;

import java.util.Collections;
import java.util.List;
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;

public interface NodeFinderInterface {

  default DelphiNode findNode(DelphiNode node) {
    return null;
  }

  default List<DelphiNode> findNodes(DelphiNode node) {
    return Collections.emptyList();
  }
}
