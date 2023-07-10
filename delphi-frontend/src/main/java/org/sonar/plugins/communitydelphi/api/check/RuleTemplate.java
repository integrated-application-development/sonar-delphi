package org.sonar.plugins.communitydelphi.api.check;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a rule class with this annotation to indicate that it's a rule template, rather than a
 * standard rule.
 *
 * @see <a
 *     href="https://docs.sonarsource.com/sonarqube/latest/user-guide/rules/overview/#rule-templates-and-custom-rules">Rule
 *     templates and custom rules</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RuleTemplate {}
