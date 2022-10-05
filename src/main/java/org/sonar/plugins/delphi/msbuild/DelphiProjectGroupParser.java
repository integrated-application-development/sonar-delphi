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
package org.sonar.plugins.delphi.msbuild;

import java.nio.file.Path;
import java.util.List;
import org.sonar.plugins.delphi.enviroment.EnvironmentVariableProvider;

final class DelphiProjectGroupParser {
  private final Path projectGroup;
  private final EnvironmentVariableProvider environmentVariableProvider;
  private final Path environmentProj;

  DelphiProjectGroupParser(
      Path projectGroup,
      EnvironmentVariableProvider environmentVariableProvider,
      Path environmentProj) {
    this.projectGroup = projectGroup;
    this.environmentVariableProvider = environmentVariableProvider;
    this.environmentProj = environmentProj;
  }

  public List<DelphiProject> parse() {
    return new DelphiMSBuildParser(projectGroup, environmentVariableProvider, environmentProj)
        .parse()
        .getProjects();
  }
}
