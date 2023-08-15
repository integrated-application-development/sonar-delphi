/*
 * Copyright (C) 2023 My Organization
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package au.com.integradev.samples.delphi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarProduct;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Context;
import org.sonar.api.server.rule.RulesDefinition.Repository;
import org.sonar.api.utils.Version;
import org.sonar.plugins.communitydelphi.api.check.MetadataResourcePath;

class MyDelphiRulesDefinitionTest {
  @Test
  void testDefine() {
    SonarRuntime runtime = mock();
    when(runtime.getApiVersion()).thenReturn(Version.create(7, 9));
    when(runtime.getProduct()).thenReturn(SonarProduct.SONARQUBE);
    when(runtime.getSonarQubeSide()).thenReturn(SonarQubeSide.SCANNER);
    when(runtime.getEdition()).thenReturn(SonarEdition.COMMUNITY);

    MetadataResourcePath metadataResourcePath = mock();
    when(metadataResourcePath.forRepository(any()))
        .thenReturn("/org/sonar/l10n/delphi/rules/" + MyDelphiRulesDefinition.REPOSITORY_KEY);

    RulesDefinition rulesDefinition = new MyDelphiRulesDefinition(runtime, metadataResourcePath);
    RulesDefinition.Context context = new Context();

    rulesDefinition.define(context);

    Repository repository = context.repository(MyDelphiRulesDefinition.REPOSITORY_KEY);
    assertThat(repository).isNotNull();
    assertThat(repository.rules()).hasSameSizeAs(RulesList.getChecks());
  }
}
