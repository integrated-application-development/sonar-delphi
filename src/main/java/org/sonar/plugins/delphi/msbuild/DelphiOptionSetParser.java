package org.sonar.plugins.delphi.msbuild;

import java.nio.file.Path;
import org.sonar.plugins.delphi.enviroment.EnvironmentVariableProvider;

class DelphiOptionSetParser extends DelphiMSBuildParser {
  private final ProjectProperties properties;

  public DelphiOptionSetParser(
      Path path,
      EnvironmentVariableProvider environmentVariableProvider,
      Path environmentProj,
      ProjectProperties properties) {
    super(path, environmentVariableProvider, environmentProj);
    this.properties = properties;
  }

  @Override
  protected ProjectProperties createProperties() {
    return properties.copy();
  }
}
