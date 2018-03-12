package org.sonar.squid.text.delphi;

public interface MetricDef {

  String getName();

  boolean aggregateIfThereIsAlreadyAValue();

}