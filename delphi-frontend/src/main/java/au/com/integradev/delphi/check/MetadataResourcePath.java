package au.com.integradev.delphi.check;

import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ServerSide
@ScannerSide
@SonarLintSide
public interface MetadataResourcePath {
  String forRepository(String repositoryKey);
}
