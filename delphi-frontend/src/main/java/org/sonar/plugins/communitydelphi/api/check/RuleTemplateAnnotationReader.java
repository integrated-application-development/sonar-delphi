package org.sonar.plugins.communitydelphi.api.check;

import java.util.List;
import java.util.Objects;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import org.sonar.api.utils.AnnotationUtils;
import org.sonar.check.Rule;

public final class RuleTemplateAnnotationReader {
  public void updateRulesByAnnotatedClass(
      RulesDefinition.NewRepository repository, List<Class<?>> ruleClasses) {
    ruleClasses.forEach(ruleClass -> handleTemplateRule(repository, ruleClass));
  }

  private static String ruleKey(Class<?> ruleClass) {
    return AnnotationUtils.getAnnotation(ruleClass, Rule.class).key();
  }

  private static boolean isTemplateRule(Class<?> ruleClass) {
    return AnnotationUtils.getAnnotation(ruleClass, RuleTemplate.class) != null;
  }

  private static void handleTemplateRule(NewRepository repository, Class<?> ruleClass) {
    NewRule rule = Objects.requireNonNull(repository.rule(ruleKey(ruleClass)));
    if (isTemplateRule(ruleClass)) {
      rule.setTemplate(true);
    } else {
      rule.params().removeIf(param -> param.key().equals("scope"));
    }
  }
}
