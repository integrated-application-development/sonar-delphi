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
      DelphiLexer.ASM, DelphiLexer.BEGIN, DelphiLexer.CASE, DelphiLexer.CLASS,
      DelphiLexer.CONST, DelphiLexer.CONSTRUCTOR, DelphiLexer.DESTRUCTOR,
      DelphiLexer.DISPINTERFACE, DelphiLexer.DIV, DelphiLexer.DO, DelphiLexer.DOWNTO,
      DelphiLexer.ELSE, DelphiLexer.EXCEPT, DelphiLexer.EXPORTS, DelphiLexer.FILE,
      DelphiLexer.FINAL, DelphiLexer.FINALIZATION, DelphiLexer.FINALLY, DelphiLexer.FOR,
      DelphiLexer.FUNCTION, DelphiLexer.GOTO, DelphiLexer.IF, DelphiLexer.IMPLEMENTATION,
      DelphiLexer.IN, DelphiLexer.INHERITED, DelphiLexer.INITIALIZATION, DelphiLexer.INLINE,
      DelphiLexer.INTERFACE, DelphiLexer.IS, DelphiLexer.LABEL, DelphiLexer.LIBRARY,
      DelphiLexer.NIL, DelphiLexer.NOT, DelphiLexer.OF, DelphiLexer.OR, DelphiLexer.OUT,
      DelphiLexer.PACKED, DelphiLexer.PROCEDURE, DelphiLexer.PROGRAM, DelphiLexer.PROPERTY,
      DelphiLexer.PROGRAM, DelphiLexer.PROPERTY, DelphiLexer.RAISE, DelphiLexer.RECORD,
      DelphiLexer.REMOVE, DelphiLexer.REPEAT, DelphiLexer.RESOURCESTRING, DelphiLexer.SEALED,
      DelphiLexer.SET, DelphiLexer.SHL, DelphiLexer.SHR, DelphiLexer.STATIC, DelphiLexer.STRICT,
      DelphiLexer.THEN, DelphiLexer.THREADVAR, DelphiLexer.TO, DelphiLexer.TRY,
      DelphiLexer.TYPE, DelphiLexer.UNIT, DelphiLexer.UNSAFE, DelphiLexer.UNTIL,
      DelphiLexer.USES, DelphiLexer.WHILE, DelphiLexer.WITH, DelphiLexer.VAR));

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
    // Checking all characters are lowercase
    String conventionRegex = "[a-z]+";
    return keywordName.matches(conventionRegex);
  }
}
