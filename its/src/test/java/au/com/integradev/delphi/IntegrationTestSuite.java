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
package au.com.integradev.delphi;

import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import com.sonar.orchestrator.locator.FileLocation;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.sonarqube.ws.Measures.ComponentWsResponse;
import org.sonarqube.ws.Measures.Measure;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.measures.ComponentRequest;

@Suite
@SelectClasses({DelphiCpdExecutorTest.class})
class IntegrationTestSuite {
  private static final String PROJECTS_PATH = "src/projects/";
  private static final FileLocation PLUGIN_LOCATION =
      FileLocation.byWildcardMavenFilename(
          new File("../sonar-delphi-plugin/target"), "sonar-delphi-plugin-*.jar");

  @RegisterExtension
  static OrchestratorExtension ORCHESTRATOR =
      OrchestratorExtension.builderEnv()
          .useDefaultAdminCredentialsForBuilds(true)
          .setSonarVersion(System.getProperty("sonar.runtimeVersion", "LATEST_RELEASE"))
          .addPlugin(PLUGIN_LOCATION)
          .build();

  private static final Path BDS = createStandardLibrary();

  private IntegrationTestSuite() {
    // Hide public constructor
  }

  private static Path createStandardLibrary() {
    try {
      Path bds = Files.createTempDirectory("bds");

      var hook = new Thread(() -> FileUtils.deleteQuietly(bds.toFile()));
      Runtime.getRuntime().addShutdownHook(hook);

      Path standardLibraryPath = Files.createDirectories(bds.resolve("source"));
      Files.writeString(
          standardLibraryPath.resolve("SysInit.pas"),
          "unit SysInit;\ninterface\nimplementation\nend.");
      Files.writeString(
          standardLibraryPath.resolve("System.pas"),
          "unit System;\n"
              + "interface\n"
              + "type\n"
              + "  TObject = class\n"
              + "  end;\n"
              + "  IInterface = interface\n"
              + "  end;\n"
              + "  TClassHelperBase = class\n"
              + "  end;\n"
              + "  TVarRec = record\n"
              + "  end;\n"
              + "implementation\n"
              + "end.");

      return bds;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static WsClient newWsClient() {
    return WsClientFactories.getDefault()
        .newClient(HttpConnector.newBuilder().url(ORCHESTRATOR.getServer().getUrl()).build());
  }

  public static Double getProjectMeasureAsDouble(String metricKey, String projectKey) {
    Measure measure = getMeasure(metricKey, projectKey);
    return (measure == null) ? null : Double.parseDouble(measure.getValue());
  }

  private static Measure getMeasure(String metricKey, String projectKey) {
    ComponentWsResponse response =
        newWsClient()
            .measures()
            .component(
                new ComponentRequest()
                    .setComponent(projectKey)
                    .setMetricKeys(Collections.singletonList(metricKey)));
    List<Measure> measures = response.getComponent().getMeasuresList();
    return measures.size() == 1 ? measures.get(0) : null;
  }

  public static SonarScanner createScanner(String projectFolder, String projectKey) {
    File projectDir = FileLocation.of(PROJECTS_PATH + projectFolder).getFile();

    return SonarScanner.create()
        .setSourceEncoding("windows-1252")
        .setProjectDir(projectDir)
        .setProjectKey(projectKey)
        .setProjectName(projectKey)
        .setProperty(DelphiProperties.BDS_PATH_KEY, BDS.toString())
        .setProjectVersion("1.0")
        .setSourceDirs("src");
  }
}
