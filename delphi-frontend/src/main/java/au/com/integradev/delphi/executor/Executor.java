/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.executor;

import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import au.com.integradev.delphi.symbol.SymbolTable;
import java.util.Collections;
import java.util.Set;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.scanner.ScannerSide;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ScannerSide
@SonarLintSide
public interface Executor {
  default void setup() {}

  void execute(Context context, DelphiInputFile delphiFile);

  default void complete() {}

  default Set<Class<? extends Executor>> dependencies() {
    return Collections.emptySet();
  }

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
