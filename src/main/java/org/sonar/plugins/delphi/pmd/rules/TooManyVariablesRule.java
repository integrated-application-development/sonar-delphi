package org.sonar.plugins.delphi.pmd.rules;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.runtime.tree.CommonTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

public class TooManyVariablesRule extends VariableCounterRule {
  private static final String VIOLATION_MESSAGE = "Too many variables: %d (max %d)";

  @Override
  public List<DelphiPMDNode> findNodes(DelphiPMDNode node) {
    if (node.getType() == DelphiLexer.TkBlockDeclSection) {
      List<?> children = node.getChildren();

      if (children != null) {
        return children.stream()
            .map(child -> new DelphiPMDNode((CommonTree) child, node.getASTTree()))
            .filter(child -> child.getType() == DelphiLexer.VAR)
            .collect(Collectors.toList());
      }
    }

    return Collections.emptyList();
  }

  @Override
  protected String getViolationMessage(int variableCount, int limit) {
    return String.format(VIOLATION_MESSAGE, variableCount, limit);
  }
}
