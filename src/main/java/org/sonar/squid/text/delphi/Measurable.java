package org.sonar.squid.text.delphi;

public interface Measurable<T extends MetricDef> {

  int getInt(T metric);

  void setMeasure(T metric, int measure);
}