package au.com.integradev.delphi;

import au.com.integradev.delphi.checks.CheckList;
import org.sonar.plugins.communitydelphi.api.check.CheckRegistrar;
import org.sonar.plugins.communitydelphi.api.check.MetadataResourcePath;
import org.sonar.plugins.communitydelphi.api.check.ScopeMetadataLoader;

public class DelphiCheckRegistrar implements CheckRegistrar {
  private final MetadataResourcePath metadataResourcePath;

  public DelphiCheckRegistrar(MetadataResourcePath metadataResourcePath) {
    this.metadataResourcePath = metadataResourcePath;
  }

  @Override
  public void register(RegistrarContext registrarContext) {
    ScopeMetadataLoader scopeMetadataLoader =
        new ScopeMetadataLoader(metadataResourcePath, getClass().getClassLoader());

    registrarContext.registerClassesForRepository(
        CheckList.REPOSITORY_KEY, CheckList.getChecks(), scopeMetadataLoader::getScope);
  }
}
