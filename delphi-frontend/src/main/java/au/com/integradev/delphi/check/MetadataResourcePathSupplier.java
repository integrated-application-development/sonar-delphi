package au.com.integradev.delphi.check;

import java.util.function.Supplier;
import org.sonar.api.scanner.ScannerSide;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ScannerSide
@SonarLintSide
@FunctionalInterface
public interface MetadataResourcePathSupplier extends Supplier<String> {}
