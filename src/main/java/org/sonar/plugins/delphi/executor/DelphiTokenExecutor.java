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

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.plugins.delphi.antlr.ast.token.DelphiToken;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiInputFile;

public abstract class DelphiTokenExecutor implements Executor {

  @Override
  public void execute(Context context, DelphiInputFile delphiFile) {
    onFile(context.sensorContext(), delphiFile);
    for (DelphiToken token : delphiFile.getTokens()) {
      handleToken(token);
    }
    save();
  }

  protected abstract void onFile(SensorContext context, DelphiInputFile file);

  protected abstract void handleToken(DelphiToken token);

  protected abstract void save();
}
