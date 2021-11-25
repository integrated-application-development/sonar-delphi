package org.sonar.plugins.delphi.coverage;

import java.util.Optional;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.coverage.delphicodecoveragetool.DelphiCodeCoverageToolParser;
import org.sonar.plugins.delphi.project.DelphiProjectHelper;

@ScannerSide
public class DelphiCoverageParserFactory {
  private static final Logger LOG = Loggers.get(DelphiCoverageParserFactory.class);

  public Optional<DelphiCoverageParser> getParser(String key, DelphiProjectHelper helper) {
    if (DelphiCodeCoverageToolParser.KEY.equals(key)) {
      return Optional.of(new DelphiCodeCoverageToolParser(helper));
    } else {
      LOG.warn("Unsupported coverage tool '{}'", key);
      return Optional.empty();
    }
  }
}
