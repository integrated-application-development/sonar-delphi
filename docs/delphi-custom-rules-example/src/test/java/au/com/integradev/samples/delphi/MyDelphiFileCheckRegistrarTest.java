/*
 * Copyright (C) 2023 My Organization
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package au.com.integradev.samples.delphi;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.check.CheckRegistrar;
import org.sonar.plugins.communitydelphi.api.check.CheckRegistrar.RegistrarContext;
import org.sonar.plugins.communitydelphi.api.check.MetadataResourcePath;

class MyDelphiFileCheckRegistrarTest {
  @Test
  void testRegister() {
    MetadataResourcePath metadataResourcePath = mock();
    when(metadataResourcePath.forRepository(any()))
        .thenReturn("org/sonar/l10n/delphi/rules/" + MyDelphiRulesDefinition.REPOSITORY_KEY);

    CheckRegistrar registrar = new MyDelphiFileCheckRegistrar(metadataResourcePath);
    RegistrarContext context = new RegistrarContext();

    registrar.register(context);

    assertThat(context.repositoryKey()).isEqualTo(MyDelphiRulesDefinition.REPOSITORY_KEY);
    assertThat(context.checkClasses()).isEqualTo(RulesList.getChecks());
  }
}
