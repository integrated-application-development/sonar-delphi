package org.sonar.plugins.delphi.pmd.rules;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class LowerCaseReservedWordsRule extends DelphiRule {

  // The keyword integers defined in DelphiLexer to check for correct convention
  private static final Set<Integer> keywords = new HashSet<>(Arrays.asList(
      DelphiLexer.ASM, DelphiLexer.BEGIN, DelphiLexer.CASE, DelphiLexer.CLASS,
      DelphiLexer.CONST, DelphiLexer.CONSTRUCTOR, DelphiLexer.DESTRUCTOR,
      DelphiLexer.DISPINTERFACE, DelphiLexer.DIV, DelphiLexer.DO, DelphiLexer.DOWNTO,
      DelphiLexer.ELSE, DelphiLexer.EXCEPT, DelphiLexer.EXPORTS, DelphiLexer.FILE,
      DelphiLexer.FINALIZATION, DelphiLexer.FINALLY, DelphiLexer.FOR,
      DelphiLexer.FUNCTION, DelphiLexer.GOTO, DelphiLexer.IF, DelphiLexer.IMPLEMENTATION,
      DelphiLexer.IN, DelphiLexer.INHERITED, DelphiLexer.INITIALIZATION, DelphiLexer.INLINE,
      DelphiLexer.INTERFACE, DelphiLexer.IS, DelphiLexer.LABEL, DelphiLexer.LIBRARY,
      DelphiLexer.NIL, DelphiLexer.NOT, DelphiLexer.OF, DelphiLexer.OR, DelphiLexer.OUT,
      DelphiLexer.PACKED, DelphiLexer.PROCEDURE, DelphiLexer.PROGRAM, DelphiLexer.PROPERTY,
      DelphiLexer.PROGRAM, DelphiLexer.PROPERTY, DelphiLexer.RAISE, DelphiLexer.RECORD,
      DelphiLexer.REPEAT, DelphiLexer.RESOURCESTRING, DelphiLexer.SEALED, DelphiLexer.SET,
      DelphiLexer.SHL, DelphiLexer.SHR, DelphiLexer.STATIC, DelphiLexer.STRICT, DelphiLexer.THEN,
      DelphiLexer.THREADVAR, DelphiLexer.TO, DelphiLexer.TRY, DelphiLexer.TYPE, DelphiLexer.UNIT,
      DelphiLexer.UNSAFE, DelphiLexer.UNTIL, DelphiLexer.USES, DelphiLexer.WHILE, DelphiLexer.WITH,
      DelphiLexer.VAR));

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    Tree parent = node.getParent();
    if (parent.getType() == DelphiLexer.TkAssemblerInstructions) {
      return;
    }

    if (!keywords.contains(node.getType())) {
      return;
    }

    String keywordName = node.getText();
    if (StringUtils.isAllLowerCase(keywordName)) {
      return;
    }

    addViolation(ctx, node);
  }
}
