package org.sonar.plugins.delphi.pmd.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.runtime.tree.CommonTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

/**
 * This rule looks at all variable names to ensure they follow basic Pascal Case. At the moment it
 * only checks if the first character is uppercase
 */
public class VariableNameRule extends NameConventionRule {

  @Override
  public List<DelphiPMDNode> findNodes(DelphiPMDNode node) {
    // Within a var block, look for variable identifiers
    if (shouldVisit(node)) {
      List<DelphiPMDNode> nameNodes = new ArrayList<>();

      for (Object child : node.getChildren()) {
        CommonTree childNode = (CommonTree) child;

        if (childNode.getType() == DelphiLexer.TkVariableIdents) {
          List<?> children = childNode.getChildren();

          nameNodes.addAll(
              children.stream()
                  .map(varName -> new DelphiPMDNode((CommonTree) varName, node.getASTTree()))
                  .collect(Collectors.toList()));
        }
      }

      if (isNotAutoCreateFormVar(node, nameNodes)) {
        return Collections.unmodifiableList(nameNodes);
      }
    }

    return Collections.emptyList();
  }

  @Override
  protected boolean isViolation(DelphiPMDNode nameNode) {
    String name = nameNode.getText();
    return !Character.isUpperCase(name.charAt(0));
  }

  private boolean shouldVisit(DelphiPMDNode node) {
    return node.getType() == DelphiLexer.VAR && node.getChildCount() != 0;
  }

  private boolean isNotAutoCreateFormVar(DelphiPMDNode node, List<DelphiPMDNode> nameNodes) {
    return !(isInterfaceSection()
        && nameNodes.size() == 1
        && node.getChildIndex() == node.getParent().getChildCount() - 1);
  }
}
