package org.sonar.plugins.delphi.pmd.rules;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class LowerCaseReservedWordsRule extends DelphiRule {

  // The keyword integers defined in DelphiLexer to check for correct convention
  private static final Set<Integer> keywords = new HashSet<>(Arrays.asList(
      DelphiLexer.NOT, DelphiLexer.OR, DelphiLexer.AND));

  /**
   * This rule checks if the above Delphi keywords are following a convention, in this case if they
   * are all lowercase. This can be changed by changing the regex in the 'checkKeyword' function
   *
   * @param node the current node
   * @param ctx the ruleContext to store the violations
   */
  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {

    int nodeType = node.getType();
    if (keywords.contains(nodeType)) {
      String keywordName = node.getText();
      if (!checkKeyword(keywordName)) {
        addViolation(ctx, node);
      }
    }

  }

  private boolean checkKeyword(String keywordName) {

    String coventionRegex = "[a-z]+"; // Checking all characters are lowercase
    return keywordName.matches(coventionRegex);
  }
}
