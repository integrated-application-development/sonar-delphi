package au.com.integradev.delphi;

import au.com.integradev.delphi.core.Delphi;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFileFilter;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.WildcardPattern;

public class DelphiExclusionsFileFilter implements InputFileFilter {
  private final Configuration settings;

  public DelphiExclusionsFileFilter(Configuration settings) {
    this.settings = settings;
  }

  @Override
  public boolean accept(InputFile inputFile) {
    if (!Delphi.KEY.equals(inputFile.language())) {
      return true;
    }
    String[] excludedPatterns = this.settings.getStringArray(DelphiProperties.EXCLUSIONS_KEY);
    String relativePath = inputFile.uri().toString();
    return !WildcardPattern.match(WildcardPattern.create(excludedPatterns), relativePath);
  }
}
