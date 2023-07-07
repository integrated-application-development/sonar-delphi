package au.com.integradev.delphi.check;

import org.sonar.api.scanner.ScannerSide;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ScannerSide
@SonarLintSide
public interface MetadataResourcePathSupplier {
  String forRepository(String repositoryKey);
}
