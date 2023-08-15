/*
 * Copyright (C) 2023 My Organization
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package au.com.integradev.samples.delphi;

import org.sonar.plugins.communitydelphi.api.check.CheckRegistrar;
import org.sonar.plugins.communitydelphi.api.check.MetadataResourcePath;
import org.sonar.plugins.communitydelphi.api.check.ScopeMetadataLoader;

/**
 * Provide the "checks" (implementations of rules) classes that are going to be executed during
 * source code analysis.
 *
 * <p>This class is a batch extension by implementing the {@link
 * org.sonar.plugins.communitydelphi.api.check.CheckRegistrar} interface.
 */
public class MyDelphiFileCheckRegistrar implements CheckRegistrar {
  private final MetadataResourcePath metadataResourcePath;

  public MyDelphiFileCheckRegistrar(MetadataResourcePath metadataResourcePath) {
    this.metadataResourcePath = metadataResourcePath;
  }

  /** Register the classes that will be used to instantiate checks during analysis. */
  @Override
  public void register(RegistrarContext registrarContext) {
    // The core plugin needs to know the scope for each rule (ALL, MAIN, TEST)
    // The ScopeMetadataLoader class can load the rule scope from the JSON rule metadata.
    ScopeMetadataLoader scopeMetadataLoader =
        new ScopeMetadataLoader(metadataResourcePath, getClass().getClassLoader());

    // Associate the classes with the correct repository key and scope.
    registrarContext.registerClassesForRepository(
        MyDelphiRulesDefinition.REPOSITORY_KEY,
        RulesList.getChecks(),
        scopeMetadataLoader::getScope);
  }
}
