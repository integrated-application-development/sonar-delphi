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
package org.sonar.plugins.delphi.extension;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.locator.FileLocation;
import java.io.File;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class OrchestratorExtension implements BeforeAllCallback, AfterAllCallback {
  private static final FileLocation PLUGIN_LOCATION =
      FileLocation.byWildcardMavenFilename(new File("target"), "sonar-delphi-plugin-*.jar");

  private final Orchestrator orchestrator =
      Orchestrator.builderEnv()
          .useDefaultAdminCredentialsForBuilds(true)
          .setSonarVersion(System.getProperty("sonar.runtimeVersion", "LATEST_RELEASE"))
          .addPlugin(PLUGIN_LOCATION)
          .build();

  @Override
  public void beforeAll(ExtensionContext context) {
    orchestrator.start();
  }

  @Override
  public void afterAll(ExtensionContext context) {
    orchestrator.stop();
  }

  public Orchestrator getOrchestrator() {
    return this.orchestrator;
  }
}
