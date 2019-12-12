package org.sonar.plugins.delphi.pmd.rules;

import static org.apache.commons.lang3.StringUtils.countMatches;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.DelphiToken;
import org.sonar.plugins.delphi.pmd.FilePosition;

public class TabulationCharactersRule extends AbstractDelphiRule {
  private static final String MESSAGE =
      "Tabulation characters should not be used (%d found in file)";

  private int tabCount;

  @Override
  public RuleContext visit(DelphiAST ast, RuleContext data) {
    tabCount = 0;
    super.visit(ast, data);

    if (tabCount > 0) {
      newViolation(data)
          .atPosition(FilePosition.atFileLevel())
          .message(String.format(MESSAGE, tabCount))
          .save();
    }

    return data;
  }

  @Override
  public void visitToken(DelphiToken token, RuleContext data) {
    if (token.isWhitespace()) {
      tabCount += countMatches(token.getImage(), '\t');
    }
  }
}
