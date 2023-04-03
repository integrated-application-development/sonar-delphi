package org.sonar.plugins.communitydelphi.api.check;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
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

  /** Context for checks registration. */
  class RegistrarContext {
    private String repositoryKey;
    private List<Class<?>> checkClasses;

    /**
     * Registers delphi checks for a given repository.
     *
     * @param repositoryKey key of rule repository
     * @param checkClasses classes of checks
     */
    public void registerClassesForRepository(
        String repositoryKey, Iterable<Class<?>> checkClasses) {
      Preconditions.checkArgument(
          StringUtils.isNotBlank(repositoryKey), "Please specify a valid repository key");
      this.repositoryKey = repositoryKey;
      this.checkClasses = ImmutableList.copyOf(checkClasses);
    }

    /**
     * Returns the repository key.
     *
     * @return repository key
     */
    public String repositoryKey() {
      return repositoryKey;
    }

    /**
     * Returns the registered check classes.
     *
     * @return registered check classes
     */
    public List<Class<?>> getCheckClasses() {
      return checkClasses;
    }
  }
}
