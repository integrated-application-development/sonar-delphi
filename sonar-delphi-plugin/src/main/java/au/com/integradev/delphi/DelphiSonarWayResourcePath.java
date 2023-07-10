package au.com.integradev.delphi;

import au.com.integradev.delphi.check.MetadataResourcePath;
import au.com.integradev.delphi.checks.CheckList;
import org.sonar.api.server.ServerSide;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ServerSide
@SonarLintSide
public class DelphiSonarWayResourcePath {
  private final MetadataResourcePath metadataResourcePath;

  public DelphiSonarWayResourcePath(MetadataResourcePath metadataResourcePath) {
    this.metadataResourcePath = metadataResourcePath;
  }

  public String get() {
    return metadataResourcePath.forRepository(CheckList.REPOSITORY_KEY) + "/Sonar_way_profile.json";
  }
}
