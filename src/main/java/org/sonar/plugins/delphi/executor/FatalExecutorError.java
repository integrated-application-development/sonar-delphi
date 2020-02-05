package org.sonar.plugins.delphi.executor;

/**
 * Typically if an exception reaches the master Executor, it gets logged and the current Executor
 * gets skipped.
 *
 * <p>As the name implies, a FatalExecutorError is something we don't want to try recovering from.
 * Instead of skipping the current Executor, it will fail the whole scan.
 */
public class FatalExecutorError extends RuntimeException {
  public FatalExecutorError(String message, Exception cause) {
    super(message, cause);
  }
}
