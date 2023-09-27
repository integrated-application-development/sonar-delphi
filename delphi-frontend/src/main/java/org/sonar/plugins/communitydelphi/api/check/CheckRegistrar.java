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

import java.util.function.Function;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.RuleScope;
import org.sonar.api.scanner.ScannerSide;
import org.sonarsource.api.sonarlint.SonarLintSide;

/**
 * This batch extension should be extended to provide the classes to be used to instantiate checks.
 * The register method has to be implemented and the registrarContext should register the repository
 * keys.
 *
 * <pre>{@code
 * public void register(RegistrarContext registrarContext) {
 *   registrarContext.registerClassesForRepository("RepositoryKey", listOfCheckClasses);
 * }
 *
 * }</pre>
 *
 * <p>Based directly on {@code CheckRegistrar} from the sonar-java project.
 *
 * @see <a
 *     href="https://github.dev/SonarSource/sonar-java/blob/master/java-frontend/src/main/java/org/sonar/plugins/java/api/CheckRegistrar.java#L40">
 *     CheckRegistrar</a>
 */
@SonarLintSide
@ScannerSide
public interface CheckRegistrar {

  /**
   * This method is called during an analysis to get the classes to use to instantiate checks.
   *
   * @param registrarContext the context that will be used by the delphi-plugin to retrieve the
   *     classes for checks.
   */
  void register(RegistrarContext registrarContext);

  interface RegistrarContext {

    /**
     * Registers delphi checks for a given repository.
     *
     * @param repositoryKey key of rule repository
     * @param checkClasses classes of checks
     * @param scopeFunction function that returns a {@code RuleScope} for a given rule's engine key
     */
    void registerClassesForRepository(
        String repositoryKey,
        Iterable<Class<?>> checkClasses,
        Function<RuleKey, RuleScope> scopeFunction);
  }
}
