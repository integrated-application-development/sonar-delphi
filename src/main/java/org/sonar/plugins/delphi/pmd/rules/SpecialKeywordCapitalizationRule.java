package org.sonar.plugins.delphi.pmd.rules;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.capitalize;

import java.util.regex.Pattern;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.DelphiToken;

public class SpecialKeywordCapitalizationRule extends AbstractDelphiRule {
  private static final Pattern PATTERN = Pattern.compile("[A-Z]{1}[a-z]+");
  private static final String MESSAGE = "Incorrect special keyword casing (did you mean '%s'?)";

  @Override
  public void visitToken(DelphiToken token, RuleContext data) {
    if (token.isSpecialKeyword() && !PATTERN.matcher(token.getImage()).matches()) {
      String correctCase = capitalize(token.getImage().toLowerCase());
      addViolation(data, token, format(MESSAGE, correctCase));
    }
  }
}
