package org.sonar.plugins.delphi.pmd.rules;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.runtime.tree.CommonTree;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class ConstantNotationRule extends NameConventionRule {

  private static final String PREFIX = "C_";

  @Override
  public List<DelphiPMDNode> findNodes(DelphiPMDNode node) {
    if (node.getType() != DelphiLexer.CONST) {
      return Collections.emptyList();
    }

    return node.findAllChildren(DelphiLexer.TkConstantName).stream()
        .map(name -> new DelphiPMDNode((CommonTree) name.getChild(0), node.getASTTree()))
        .collect(Collectors.toList());
  }

  @Override
  protected boolean isViolation(DelphiPMDNode node) {
    return !compliesWithPrefixNamingConvention(node.getText(), PREFIX);
  }
}
