package org.sonar.plugins.communitydelphi.api.directive;

public interface WarnDirective extends ParameterDirective {
  enum WarnParameterValue {
    ON,
    OFF,
    ERROR,
    DEFAULT
  }

  String getIdentifier();

  WarnParameterValue getValue();
}
