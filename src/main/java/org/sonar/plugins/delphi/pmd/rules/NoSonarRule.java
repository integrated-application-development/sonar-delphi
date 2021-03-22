package org.sonar.plugins.delphi.pmd.rules;

import java.util.regex.Pattern;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.token.DelphiToken;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;

public class NoSonarRule extends AbstractDelphiRule {
  private static final Pattern PATTERN =
      Pattern.compile(String.format(".*\\b%s\\b.*", DelphiPmdConstants.SUPPRESSION_TAG));

  @Override
  public void visitToken(DelphiToken token, RuleContext data) {
    if (token.isComment() && PATTERN.matcher(token.getImage()).matches()) {
      addViolation(data, token);
    }
  }

  @Override
  public String getMessage() {
    return "Is //NOSONAR used to exclude false-positives or to hide real quality flaw?";
  }

  @Override
  public boolean isSuppressedLine(int line) {
    return false;
  }
}
