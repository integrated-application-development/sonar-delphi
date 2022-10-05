/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package org.sonar.plugins.delphi.executor;

import java.util.Collections;
import java.util.Set;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiInputFile;
import org.sonar.plugins.delphi.symbol.SymbolTable;

@ScannerSide
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
