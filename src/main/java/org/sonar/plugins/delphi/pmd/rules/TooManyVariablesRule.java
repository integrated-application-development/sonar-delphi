package org.sonar.plugins.delphi.pmd.rules;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

public class TooManyVariablesRule extends VariableCounterRule {
  private static final String VIOLATION_MESSAGE = "Too many variables: %d (max %d)";

  @Override
  public List<DelphiNode> findNodes(DelphiNode node) {
    if (node.getType() == DelphiLexer.TkFunctionName) {
      DelphiNode method = (DelphiNode) node.getParent();
      DelphiNode nextNode = method.nextNode();

      if (nextNode != null && nextNode.getType() == DelphiLexer.TkBlockDeclSection) {
        List<?> children = nextNode.getChildren();
        if (children != null) {
          return children.stream()
              .map(child -> (DelphiNode) child)
              .filter(child -> child.getType() == DelphiLexer.VAR)
              .collect(Collectors.toList());
        }
      }
    }

    return Collections.emptyList();
  }

  @Override
  protected String getViolationMessage(int variableCount, int limit) {
    return String.format(VIOLATION_MESSAGE, variableCount, limit);
  }
}
