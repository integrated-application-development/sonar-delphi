package au.com.integradev.delphi.check;

import org.sonar.plugins.communitydelphi.api.check.MetadataResourcePath;

public class MetadataResourcePathImpl implements MetadataResourcePath {
  @Override
  public String forRepository(String repositoryKey) {
    return "org/sonar/l10n/delphi/rules/" + repositoryKey;
  }
}
