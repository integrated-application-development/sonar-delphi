package org.sonar.plugins.delphi;

import static java.util.Collections.singletonList;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.locator.FileLocation;
import java.io.File;
import java.util.List;
import org.junit.ClassRule;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonarqube.ws.Measures.ComponentWsResponse;
import org.sonarqube.ws.Measures.Measure;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.measures.ComponentRequest;

@SuppressWarnings("JUnit5Platform")
@RunWith(JUnitPlatform.class)
@IncludeClassNamePatterns("^.*IT$")
@SelectClasses({DelphiCpdExecutorIT.class})
public class IntegrationTestSuite {
  private static final String PROJECTS_PATH = "src/it/projects/";
  private static final String STANDARD_LIBRARY_PATH =
      DelphiUtils.getResource("/org/sonar/plugins/delphi/standardLibrary").getAbsolutePath();

  private static final FileLocation PLUGIN_LOCATION =
      FileLocation.byWildcardMavenFilename(new File("target"), "sonar-delphi-plugin-*.jar");

  @ClassRule
  public static final Orchestrator ORCHESTRATOR =
      Orchestrator.builderEnv()
          .setSonarVersion(System.getProperty("sonar.runtimeVersion", "LATEST_RELEASE"))
          .addPlugin(PLUGIN_LOCATION)
          .build();

  private IntegrationTestSuite() {
    // Hide public constructor
  }

  public static Orchestrator getOrchestrator() {
    return ORCHESTRATOR;
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
                    .setMetricKeys(singletonList(metricKey)));
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
        .setProperty(DelphiPlugin.STANDARD_LIBRARY_KEY, STANDARD_LIBRARY_PATH)
        .setProjectVersion("1.0")
        .setSourceDirs("src");
  }
}