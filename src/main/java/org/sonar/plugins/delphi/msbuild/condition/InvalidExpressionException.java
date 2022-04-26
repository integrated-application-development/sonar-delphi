package org.sonar.plugins.delphi.msbuild.condition;

public class InvalidExpressionException extends RuntimeException {
  InvalidExpressionException(String message) {
    super(message);
  }
}
