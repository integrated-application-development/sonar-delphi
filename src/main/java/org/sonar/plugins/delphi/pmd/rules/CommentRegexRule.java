package org.sonar.plugins.delphi.pmd.rules;

import java.util.regex.Pattern;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.antlr.runtime.Token;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class CommentRegexRule extends DelphiRule {
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
    String regularExpression = getProperty(REGEX);

    try {
      pattern = Pattern.compile(regularExpression, Pattern.DOTALL);
    } catch (RuntimeException e) {
      pattern = null;
      LOG.error("Unable to compile regular expression: " + regularExpression, e);
    }
  }

  @Override
  public void visitComment(Token comment, RuleContext ctx) {
    if (pattern != null && pattern.matcher(comment.getText()).matches()) {
      addViolation(ctx, comment, getProperty(MESSAGE));
    }
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
