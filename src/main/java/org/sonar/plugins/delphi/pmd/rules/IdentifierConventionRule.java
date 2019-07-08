package org.sonar.plugins.delphi.pmd.rules;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.runtime.tree.CommonTree;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * This rule looks at all variable names to ensure they follow basic Pascal Case.
 * At the moment it only checks if the first character is uppercase
 */
public class IdentifierConventionRule extends NameConventionRule {

  @Override
  public List<DelphiPMDNode> findNameNodes(DelphiPMDNode node) {
    // Within a var block, look for variable identifiers
    if (node.getType() == DelphiLexer.VAR) {
      for (Object child : node.getChildren()) {
        CommonTree childNode = (CommonTree) child;

        if (childNode.getType() == DelphiLexer.TkVariableIdents) {
          List<?> children = childNode.getChildren();

          return children.stream()
              .map(varName -> new DelphiPMDNode((CommonTree) varName, node.getASTTree()))
              .collect(Collectors.toList());
        }
      }
    }

    return Collections.emptyList();
  }

  @Override
  protected boolean isViolation(DelphiPMDNode nameNode) {
    String name = nameNode.getText();
    return !Character.isUpperCase(name.charAt(0));
  }
}

