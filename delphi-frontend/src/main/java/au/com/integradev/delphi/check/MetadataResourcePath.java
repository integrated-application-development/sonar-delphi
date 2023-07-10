package au.com.integradev.delphi.check;

import org.sonar.api.scanner.ScannerSide;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ScannerSide
@SonarLintSide
public interface MetadataResourcePath {
  String forRepository(String repositoryKey);
}
