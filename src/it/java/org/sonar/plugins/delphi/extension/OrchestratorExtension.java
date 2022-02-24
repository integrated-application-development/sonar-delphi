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
