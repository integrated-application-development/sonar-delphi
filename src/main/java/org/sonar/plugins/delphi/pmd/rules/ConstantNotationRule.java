package org.sonar.plugins.delphi.pmd.rules;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

public class ConstantNotationRule extends NameConventionRule {

  private static final String PREFIX = "C_";

  @Override
  public List<DelphiNode> findNodes(DelphiNode node) {
    if (node.getType() != DelphiLexer.CONST) {
      return Collections.emptyList();
    }

    return node.findAllChildren(DelphiLexer.TkConstantName).stream()
        .map(name -> (DelphiNode) name.getChild(0))
        .collect(Collectors.toList());
  }

  @Override
  protected boolean isViolation(DelphiNode node) {
    return !compliesWithPrefixNamingConvention(node.getText(), PREFIX);
  }
}
