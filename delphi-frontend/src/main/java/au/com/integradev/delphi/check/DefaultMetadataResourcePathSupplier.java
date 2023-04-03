package au.com.integradev.delphi.check;

public class DefaultMetadataResourcePathSupplier implements MetadataResourcePathSupplier {
  @Override
  public String get() {
    return "org/sonar/l10n/delphi/rules/delphi";
  }
}
