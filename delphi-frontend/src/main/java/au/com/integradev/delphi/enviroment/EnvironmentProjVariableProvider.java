/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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
package au.com.integradev.delphi.enviroment;

import au.com.integradev.delphi.msbuild.MSBuildParser;
import au.com.integradev.delphi.msbuild.MSBuildState;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

public class EnvironmentProjVariableProvider implements EnvironmentVariableProvider {
  private final MSBuildState state;

  public EnvironmentProjVariableProvider(
      Path environmentProj, EnvironmentVariableProvider baseProvider) {
    if (environmentProj != null && Files.exists(environmentProj)) {
      var parser = new MSBuildParser(environmentProj, baseProvider);
      state = parser.parse();
    } else {
      state =
          new MSBuildState(
              environmentProj, environmentProj, baseProvider.getenv(), Collections.emptyMap());
    }
  }

  @Override
  public Map<String, String> getenv() {
    return state.getProperties();
  }

  @Override
  public String getenv(String name) {
    return state.getProperty(name);
  }
}
