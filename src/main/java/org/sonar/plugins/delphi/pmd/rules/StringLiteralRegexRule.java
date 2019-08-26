package org.sonar.plugins.delphi.pmd.rules;

import java.util.regex.Pattern;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.antlr.ast.node.LiteralNode;

public class StringLiteralRegexRule extends AbstractDelphiRule {
  private static final Logger LOG = Loggers.get(StringLiteralRegexRule.class);

  public static final PropertyDescriptor<String> REGEX =
      PropertyFactory.stringProperty("regex")
          .desc("The regular expression")
          .defaultValue("(?!)")
          .build();

  private static final PropertyDescriptor<String> MESSAGE =
      PropertyFactory.stringProperty("message").desc("The issue message").defaultValue("").build();

  private Pattern pattern;

  public StringLiteralRegexRule() {
    definePropertyDescriptor(REGEX);
    definePropertyDescriptor(MESSAGE);
  }

  @Override
  public void start(RuleContext ctx) {
    String regularExpression = getProperty(REGEX);

    try {
      pattern = Pattern.compile(regularExpression, Pattern.DOTALL);
    } catch (IllegalArgumentException e) {
      LOG.debug("Unable to compile regular expression: " + regularExpression, e);
    }
  }

  @Override
  public RuleContext visit(LiteralNode literal, RuleContext data) {
    if (literal.isStringLiteral()) {
      String string = literal.getImage().substring(1, literal.getImage().length() - 1);

      if (pattern != null && pattern.matcher(string).matches()) {
        addViolationWithMessage(data, literal, getProperty(MESSAGE));
      }
    }

    return super.visit(literal, data);
  }

  @Override
  public String dysfunctionReason() {
    start(null);
    return pattern == null ? ("Unable to compile regular expression: " + getProperty(REGEX)) : null;
  }
}
