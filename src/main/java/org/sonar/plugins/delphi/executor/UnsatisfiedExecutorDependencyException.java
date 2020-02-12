package org.sonar.plugins.delphi.executor;

public class UnsatisfiedExecutorDependencyException extends RuntimeException {
  public UnsatisfiedExecutorDependencyException(
      Executor executor, Class<? extends Executor> dependency) {
    super(createMessage(executor, dependency));
  }

  public UnsatisfiedExecutorDependencyException(
      Executor executor, Class<? extends Executor> dependency, Exception cause) {
    super(createMessage(executor, dependency), cause);
  }

  private static String createMessage(Executor executor, Class<? extends Executor> dependency) {
    return String.format(
        "Unsatisfied executor dependency: %s depends on the execution of %s",
        executor.getClass().getSimpleName(), dependency.getSimpleName());
  }
}
