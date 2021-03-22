package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.BASE_EFFORT;
import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.LIMIT;
import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.SCOPE;
import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.TEMPLATE;
import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.TYPE;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.rule.ImmutableLanguage;
import org.sonar.plugins.delphi.antlr.ast.token.DelphiToken;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.pmd.FilePosition;
import org.sonar.plugins.delphi.pmd.violation.DelphiRuleViolationBuilder;

public interface DelphiRule extends Rule, DelphiParserVisitor<RuleContext>, ImmutableLanguage {

  /**
   * Returns true if the line should have issues suppressed
   *
   * @param line the line to check
   * @return true if the line should have issues suppressed
   * @see org.sonar.plugins.delphi.pmd.DelphiPmdConstants#SUPPRESSION_TAG
   */
  boolean isSuppressedLine(int line);

  default void defineBaseProperties() {
    definePropertyDescriptor(BASE_EFFORT);
    definePropertyDescriptor(LIMIT);
    definePropertyDescriptor(SCOPE);
    definePropertyDescriptor(TEMPLATE);
    definePropertyDescriptor(TYPE);
  }

  /**
   * Adds violation to pmd report for a token
   *
   * @param data Visitor data (RuleContext)
   * @param token Violation token
   */
  default void addViolation(Object data, DelphiToken token) {
    newViolation(data).atPosition(FilePosition.from(token)).save();
  }

  /**
   * Adds violation to pmd report for a token (with override message)
   *
   * @param data Visitor data (RuleContext)
   * @param token Violation token
   * @param msg Violation message
   */
  default void addViolation(Object data, DelphiToken token, String msg) {
    newViolation(data).atPosition(FilePosition.from(token)).message(msg).save();
  }

  default DelphiRuleViolationBuilder newViolation(Object data) {
    return DelphiRuleViolationBuilder.newViolation(this, (RuleContext) data);
  }
}
