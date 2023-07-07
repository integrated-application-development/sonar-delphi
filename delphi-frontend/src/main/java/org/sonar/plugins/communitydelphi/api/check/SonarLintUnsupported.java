package org.sonar.plugins.communitydelphi.api.check;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a rule class with this annotation to indicate that it's unsupported on the SonarLint
 * platform.
 *
 * <p>This is typically because the rule requires information that is only available in a full scan.
 * For example, complete symbol usage data.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SonarLintUnsupported {}
