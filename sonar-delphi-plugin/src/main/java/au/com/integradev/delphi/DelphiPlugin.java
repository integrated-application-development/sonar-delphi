/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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

import au.com.integradev.delphi.check.MasterCheckRegistrar;
import au.com.integradev.delphi.check.MetadataResourcePathImpl;
import au.com.integradev.delphi.core.Delphi;
import au.com.integradev.delphi.core.DelphiFeatures;
import au.com.integradev.delphi.core.DelphiFeatures.Feature;
import au.com.integradev.delphi.coverage.DelphiCoverageParserFactory;
import au.com.integradev.delphi.enviroment.DefaultEnvironmentVariableProvider;
import au.com.integradev.delphi.executor.DelphiChecksExecutor;
import au.com.integradev.delphi.executor.DelphiCpdExecutor;
import au.com.integradev.delphi.executor.DelphiHighlightExecutor;
import au.com.integradev.delphi.executor.DelphiIssueResolutionExecutor;
import au.com.integradev.delphi.executor.DelphiMasterExecutor;
import au.com.integradev.delphi.executor.DelphiMetricsExecutor;
import au.com.integradev.delphi.executor.DelphiNoSonarExecutor;
import au.com.integradev.delphi.executor.DelphiSymbolTableExecutor;
import au.com.integradev.delphi.msbuild.DelphiProjectHelper;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.api.Plugin;
import org.sonar.api.SonarProduct;
import org.sonar.api.utils.Version;

public class DelphiPlugin implements Plugin {
  static final Version ISSUE_RESOLUTION_MIN_API_VERSION = Version.create(13, 5);

  private static final Map<Feature, List<String>> FEATURE_PROPERTIES =
      Map.of(
          Feature.LANGUAGE_REGISTRATION, List.of(Delphi.FILE_SUFFIXES_KEY),
          Feature.COVERAGE_IMPORT, List.of(DelphiProperties.COVERAGE_REPORT_KEY),
          Feature.TEST_EXECUTION_IMPORT, List.of(DelphiProperties.NUNIT_REPORT_PATHS_PROPERTY),
          Feature.TOOLCHAIN,
              List.of(
                  DelphiProperties.INSTALLATION_PATH_KEY,
                  DelphiProperties.COMPILER_TOOLCHAIN_KEY,
                  DelphiProperties.COMPILER_VERSION_KEY),
          Feature.PROJECT_OPTIONS,
              List.of(
                  DelphiProperties.SEARCH_PATH_KEY,
                  DelphiProperties.CODE_PAGE_KEY,
                  DelphiProperties.CONDITIONAL_DEFINES_KEY,
                  DelphiProperties.CONDITIONAL_UNDEFINES_KEY,
                  DelphiProperties.UNIT_SCOPE_NAMES_KEY,
                  DelphiProperties.UNIT_ALIASES_KEY));

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  @Override
  public void define(Context context) {
    var features = DelphiIntegration.integrate(context, getClass().getClassLoader());
    ImmutableList.Builder<Object> builder = ImmutableList.builder();

    Set<String> omittedProperties =
        FEATURE_PROPERTIES.entrySet().stream()
            .filter(entry -> !features.isEnabled(entry.getKey()))
            .flatMap(entry -> entry.getValue().stream())
            .collect(Collectors.toSet());
    DelphiProperties.getProperties().stream()
        .filter(property -> !omittedProperties.contains(property.key()))
        .forEach(builder::add);

    builder.add(
        // Core
        features,
        DelphiExclusionsFileFilter.class,
        // Checks
        DelphiRulesDefinition.class,
        MasterCheckRegistrar.class,
        DelphiCheckRegistrar.class,
        // Sensors
        DelphiSensor.class,
        // Executors
        DelphiMasterExecutor.class,
        DelphiSymbolTableExecutor.class,
        DelphiChecksExecutor.class,
        DelphiNoSonarExecutor.class,
        // Core helpers
        DelphiProjectHelper.class,
        // Environment
        DefaultEnvironmentVariableProvider.class,
        // Paths
        MetadataResourcePathImpl.class,
        DelphiSonarWayResourcePath.class);

    if (features.isEnabled(Feature.LANGUAGE_REGISTRATION)) {
      builder.add(Delphi.class);
    }

    if (context.getRuntime().getProduct() == SonarProduct.SONARQUBE) {
      addSonarQubeExtensions(context, builder, features);
    }

    context.addExtensions(builder.build());
  }

  private static void addSonarQubeExtensions(
      Context context, ImmutableList.Builder<Object> builder, DelphiFeatures features) {
    if (features.isEnabled(Feature.BUILT_IN_QUALITY_PROFILE)) {
      builder.add(DelphiSonarWayProfile.class);
    }
    if (features.isEnabled(Feature.COVERAGE_IMPORT)) {
      builder.add(DelphiCoverageSensor.class, DelphiCoverageParserFactory.class);
    }
    if (features.isEnabled(Feature.TEST_EXECUTION_IMPORT)) {
      builder.add(DelphiNUnitSensor.class);
    }
    if (features.isEnabled(Feature.DUPLICATION_TOKENS)) {
      builder.add(DelphiCpdExecutor.class);
    }
    if (features.isEnabled(Feature.SYNTAX_HIGHLIGHTING)) {
      builder.add(DelphiHighlightExecutor.class);
    }
    if (features.isEnabled(Feature.FILE_METRICS) || features.isEnabled(Feature.NCLOC_DATA)) {
      builder.add(DelphiMetricsExecutor.class);
    }
    if (features.isEnabled(Feature.SONAR_RESOLVE)
        && context
            .getRuntime()
            .getApiVersion()
            .isGreaterThanOrEqual(ISSUE_RESOLUTION_MIN_API_VERSION)) {
      builder.add(DelphiIssueResolutionExecutor.class);
    }
  }
}
