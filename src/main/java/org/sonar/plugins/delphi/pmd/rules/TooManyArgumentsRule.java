package org.sonar.plugins.delphi.pmd.rules;

import org.antlr.runtime.tree.CommonTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

public class TooManyArgumentsRule extends VariableCounterRule {

  private static final String VIOLATION_MESSAGE = "Too many arguments: %d (max %d)";

  @Override
  public DelphiPMDNode findNode(DelphiPMDNode node) {
    if (node.getType() != DelphiLexer.TkFunctionName) {
      return null;
    }

    CommonTree parent = (CommonTree) node.getParent();
    CommonTree args = (CommonTree) parent.getFirstChildWithType(DelphiLexer.TkFunctionArgs);

    return new DelphiPMDNode(args, node.getASTTree());
  }

  @Override
  protected String getViolationMessage(int variableCount, int limit) {
    return String.format(VIOLATION_MESSAGE, variableCount, limit);
  }
}
