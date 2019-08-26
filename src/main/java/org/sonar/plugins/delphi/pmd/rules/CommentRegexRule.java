package org.sonar.plugins.delphi.pmd.rules;

import java.util.regex.Pattern;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.antlr.ast.DelphiToken;

public class CommentRegexRule extends AbstractDelphiRule {
  private static final Logger LOG = Loggers.get(CommentRegexRule.class);

  public static final PropertyDescriptor<String> REGEX =
      PropertyFactory.stringProperty("regex")
          .desc("The regular expression")
          .defaultValue("(?!)")
          .build();

  private static final PropertyDescriptor<String> MESSAGE =
      PropertyFactory.stringProperty("message").desc("The issue message").defaultValue("").build();

  private Pattern pattern;

  public CommentRegexRule() {
    definePropertyDescriptor(REGEX);
    definePropertyDescriptor(MESSAGE);
  }

  @Override
  public void start(RuleContext ctx) {
    if (pattern == null) {
      String regularExpression = getProperty(REGEX);

      try {
        pattern = Pattern.compile(regularExpression, Pattern.DOTALL);
      } catch (IllegalArgumentException e) {
        LOG.debug("Unable to compile regular expression: " + regularExpression, e);
      }
    }
  }

  @Override
  public void visitToken(DelphiToken token, RuleContext data) {
    if (token.isComment() && pattern.matcher(token.getImage()).matches()) {
      addViolation(data, token, getProperty(MESSAGE));
    }
  }

  @Override
  public String dysfunctionReason() {
    start(null);
    return pattern == null ? ("Unable to compile regular expression: " + getProperty(REGEX)) : null;
  }
}
