package au.com.integradev.delphi.check;

public class DefaultMetadataResourcePathSupplier implements MetadataResourcePathSupplier {
  @Override
  public String forRepository(String repositoryKey) {
    return "/org/sonar/l10n/delphi/rules/" + repositoryKey;
  }
}
