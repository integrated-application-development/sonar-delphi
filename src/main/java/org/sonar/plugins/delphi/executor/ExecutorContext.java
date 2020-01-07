package org.sonar.plugins.delphi.executor;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.plugins.delphi.symbol.SymbolTable;

public class ExecutorContext implements Executor.Context {

  private final SensorContext sensorContext;
  private final SymbolTable symbolTable;

  public ExecutorContext(SensorContext sensorContext, SymbolTable symbolTable) {
    this.sensorContext = sensorContext;
    this.symbolTable = symbolTable;
  }

  @Override
  public SensorContext sensorContext() {
    return sensorContext;
  }

  @Override
  public SymbolTable symbolTable() {
    return symbolTable;
  }
}
