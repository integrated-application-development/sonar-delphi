/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.communitydelphi.api.check;

import java.util.List;
import java.util.Objects;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import org.sonar.api.utils.AnnotationUtils;
import org.sonar.check.Rule;

/**
 * Utility class for reading and processing rule template annotations.
 *
 * <p>This class handles the configuration of SonarQube rules based on annotations found on rule
 * classes, particularly {@link RuleTemplate} annotations.
 */
public final class RuleTemplateAnnotationReader {

  /**
   * Updates rule definitions in the repository based on rule template annotations.
   *
   * @param repository the rules repository to update
   * @param ruleClasses the list of rule classes to process
   * @throws IllegalArgumentException if repository or ruleClasses is null
   */
  public void updateRulesByAnnotatedClass(
      RulesDefinition.NewRepository repository, List<Class<?>> ruleClasses) {
    if (repository == null) {
      throw new IllegalArgumentException("Repository cannot be null");
    }
    if (ruleClasses == null) {
      throw new IllegalArgumentException("Rule classes cannot be null");
    }

    ruleClasses.forEach(ruleClass -> processTemplateRule(repository, ruleClass));
  }

  private static void processTemplateRule(NewRepository repository, Class<?> ruleClass) {
    String key = extractRuleKey(ruleClass);
    NewRule rule = Objects.requireNonNull(repository.rule(key), "Rule not found for key: " + key);

    if (isTemplateRule(ruleClass)) {
      rule.setTemplate(true);
    } else {
      // Remove scope parameter for non-template rules
      rule.params().removeIf(param -> "scope".equals(param.key()));
    }
  }

  private static String extractRuleKey(Class<?> ruleClass) {
    Rule ruleAnnotation = AnnotationUtils.getAnnotation(ruleClass, Rule.class);
    if (ruleAnnotation == null) {
      throw new IllegalArgumentException(
          "Rule class must have @Rule annotation: " + ruleClass.getName());
    }
    return ruleAnnotation.key();
  }

  private static boolean isTemplateRule(Class<?> ruleClass) {
    return AnnotationUtils.getAnnotation(ruleClass, RuleTemplate.class) != null;
  }
}
