/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi;

import au.com.integradev.delphi.checks.CheckList;
import au.com.integradev.delphi.core.Delphi;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.api.SonarProduct;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.AnnotationUtils;
import org.sonar.plugins.communitydelphi.api.check.MetadataResourcePath;
import org.sonar.plugins.communitydelphi.api.check.RuleTemplateAnnotationReader;
import org.sonar.plugins.communitydelphi.api.check.SonarLintUnsupported;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

public class DelphiRulesDefinition implements RulesDefinition {
  private final SonarRuntime runtime;
  private final MetadataResourcePath metadataResourcePath;
  private final DelphiSonarWayResourcePath sonarWayResourcePath;

  public DelphiRulesDefinition(
      SonarRuntime runtime,
      MetadataResourcePath metadataResourcePath,
      DelphiSonarWayResourcePath sonarWayResourcePath) {
    this.runtime = runtime;
    this.metadataResourcePath = metadataResourcePath;
    this.sonarWayResourcePath = sonarWayResourcePath;
  }

  @Override
  public void define(Context context) {
    NewRepository repository =
        context.createRepository(CheckList.REPOSITORY_KEY, Delphi.KEY).setName("Community Delphi");

    RuleMetadataLoader ruleMetadataLoader =
        new RuleMetadataLoader(
            metadataResourcePath.forRepository(repository.key()),
            sonarWayResourcePath.get(),
            runtime);

    RuleTemplateAnnotationReader ruleTemplateAnnotationReader = new RuleTemplateAnnotationReader();

    List<Class<?>> checks =
        CheckList.getChecks().stream()
            .filter(this::isCheckSupportedOnPlatform)
            .collect(Collectors.toList());

    ruleMetadataLoader.addRulesByAnnotatedClass(repository, checks);
    ruleTemplateAnnotationReader.updateRulesByAnnotatedClass(repository, checks);

    repository.done();
  }

  private boolean isCheckSupportedOnPlatform(Class<?> check) {
    return runtime.getProduct() == SonarProduct.SONARQUBE
        || AnnotationUtils.getAnnotation(check, SonarLintUnsupported.class) == null;
  }
}
