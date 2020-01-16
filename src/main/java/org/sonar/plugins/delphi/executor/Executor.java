package org.sonar.plugins.delphi.executor;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiInputFile;
import org.sonar.plugins.delphi.symbol.SymbolTable;

@ScannerSide
public interface Executor {
  default void setup() {}

  void execute(Context context, DelphiInputFile delphiFile);

  default void complete() {}

  interface Context {
    /**
     * Returns the sensor context
     *
     * @return Sensor context
     */
    SensorContext sensorContext();

    /**
     * Returns the top-level global scope of the symbol table
     *
     * @return The global scope of the symbol table
     */
    SymbolTable symbolTable();
  }
}
