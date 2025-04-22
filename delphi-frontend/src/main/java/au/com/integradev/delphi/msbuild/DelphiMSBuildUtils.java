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
package au.com.integradev.delphi.msbuild;

import au.com.integradev.delphi.enviroment.EnvironmentVariableProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DelphiMSBuildUtils {
  private static final Logger LOG = LoggerFactory.getLogger(DelphiMSBuildUtils.class);

  private DelphiMSBuildUtils() {
    // Utility class
  }

  public static List<Path> getSourceFiles(MSBuildState state) {
    return state.getItems("DCCReference").stream()
        .filter(item -> item.getMetadata("Extension").equalsIgnoreCase(".pas"))
        .map(MSBuildItem::getPath)
        .filter(path -> regularFileExists(path, "DCCReference"))
        .collect(Collectors.toList());
  }

  public static List<DelphiProject> getProjects(
      MSBuildState state, EnvironmentVariableProvider environmentVariableProvider) {
    return state.getItems("Projects").stream()
        .map(MSBuildItem::getPath)
        .filter(path -> regularFileExists(path, "Projects"))
        .map(
            path ->
                new DelphiProjectFactory()
                    .createProject(new MSBuildParser(path, environmentVariableProvider).parse()))
        .collect(Collectors.toList());
  }

  private static boolean regularFileExists(Path path, String specifiedBy) {
    if (Files.exists(path) && Files.isRegularFile(path)) {
      return true;
    } else {
      LOG.warn("File specified by {} does not exist: {}", specifiedBy, path);
      return false;
    }
  }
}
