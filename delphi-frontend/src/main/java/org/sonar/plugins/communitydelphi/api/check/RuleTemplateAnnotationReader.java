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
