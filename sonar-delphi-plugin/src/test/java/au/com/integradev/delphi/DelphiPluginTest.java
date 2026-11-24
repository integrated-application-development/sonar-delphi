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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import au.com.integradev.delphi.check.MetadataResourcePathImpl;
import au.com.integradev.delphi.checks.CheckList;
import au.com.integradev.delphi.core.Delphi;
import au.com.integradev.delphi.core.DelphiFeatures;
import au.com.integradev.delphi.core.DelphiFeatures.Feature;
import au.com.integradev.delphi.coverage.DelphiCoverageParserFactory;
import au.com.integradev.delphi.executor.DelphiCpdExecutor;
import au.com.integradev.delphi.executor.DelphiHighlightExecutor;
import au.com.integradev.delphi.executor.DelphiIssueResolutionExecutor;
import au.com.integradev.delphi.executor.DelphiMetricsExecutor;
import au.com.integradev.delphi.executor.DelphiNoSonarExecutor;
import com.sonarsource.plugins.dre.api.v1.SonarDelphiExtensionApi;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.Plugin;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.Version;

class DelphiPluginTest {
  private static final Version OLD_PLUGIN_API_VERSION = Version.parse("9.14.0.375");

  private DelphiPlugin plugin;

  @BeforeEach
  void setUp() {
    plugin = new DelphiPlugin();
  }

  @Test
  void testToString() {
    assertThat(plugin).hasToString("DelphiPlugin");
  }

  @Test
  void testExtensionsWithoutIssueResolutionOnOlderRuntime() {
    SonarRuntime runtime =
        SonarRuntimeImpl.forSonarQube(
            OLD_PLUGIN_API_VERSION, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY);

    Plugin.Context context = new Plugin.Context(runtime);
    plugin.define(context);

    assertThat(getExtensions(context))
        .hasSize(37)
        .doesNotContain(DelphiIssueResolutionExecutor.class);
  }

  @Test
  void testExtensionsWithIssueResolutionOnSupportedRuntime() {
    SonarRuntime runtime =
        SonarRuntimeImpl.forSonarQube(
            DelphiPlugin.ISSUE_RESOLUTION_MIN_API_VERSION,
            SonarQubeSide.SCANNER,
            SonarEdition.COMMUNITY);

    Plugin.Context context = new Plugin.Context(runtime);
    plugin.define(context);

    assertThat(getExtensions(context)).hasSize(38).contains(DelphiIssueResolutionExecutor.class);
  }

  @Test
  void testMissingIntegrationApiLeavesFeaturesEnabled() {
    var context = newPluginContext();
    ClassLoader bootstrapOnly = new ClassLoader(null) {};

    var features = DelphiIntegration.integrate(context, bootstrapOnly);
    assertThat(Feature.values()).allMatch(features::isEnabled);
    assertThat(getExtensions(context)).isEmpty();
  }

  @Test
  void testIntegrationContributesSonarWayRulesExceptCognitiveComplexity() {
    var context = newPluginContext();
    var profileContext = new BuiltInQualityProfilesDefinition.Context();
    var resourcePath = new DelphiSonarWayResourcePath(new MetadataResourcePathImpl());
    new DelphiSonarWayProfile(resourcePath).define(profileContext);
    var rules =
        profileContext.profile(Delphi.KEY, "Sonar way").rules().stream()
            .map(rule -> RuleKey.of(rule.repoKey(), rule.ruleKey()))
            .collect(Collectors.toSet());
    assertThat(rules).doesNotContain(RuleKey.of(CheckList.REPOSITORY_KEY, "ParsingError"));
    assertThat(rules.remove(RuleKey.of(CheckList.REPOSITORY_KEY, "CognitiveComplexityRoutine")))
        .isTrue();

    try (var api = mockStatic(SonarDelphiExtensionApi.class)) {
      api.when(() -> SonarDelphiExtensionApi.integrate(any(), any())).thenReturn(Set.of());

      plugin.define(context);

      api.verify(() -> SonarDelphiExtensionApi.integrate(context, rules));
      api.verifyNoMoreInteractions();
    }
  }

  @ParameterizedTest
  @MethodSource("featureExtensions")
  void testOnlyRequestedFeatureIsOmitted(String feature, Object[] omittedExtensions) {
    var standalone = newPluginContext();
    plugin.define(standalone);
    var integrated = newPluginContext();
    try (var api = mockStatic(SonarDelphiExtensionApi.class)) {
      api.when(() -> SonarDelphiExtensionApi.integrate(any(), any())).thenReturn(Set.of(feature));

      plugin.define(integrated);
    }

    var omitted = Arrays.asList(omittedExtensions);
    var negotiatedFeature = Feature.valueOf(feature);
    assertThat(getExtensions(standalone))
        .filteredOn(DelphiFeatures.class::isInstance)
        .extracting(extension -> ((DelphiFeatures) extension).isEnabled(negotiatedFeature))
        .containsExactly(true);
    assertThat(getExtensions(integrated))
        .filteredOn(DelphiFeatures.class::isInstance)
        .extracting(extension -> ((DelphiFeatures) extension).isEnabled(negotiatedFeature))
        .containsExactly(false);
    assertThat(extensionKeys(standalone)).containsAll(omitted);
    assertThat(extensionKeys(integrated))
        .containsExactlyElementsOf(
            extensionKeys(standalone).stream()
                .filter(extension -> !omitted.contains(extension))
                .collect(Collectors.toList()));
  }

  @Test
  void testIgnoresNoSonarFeatureFromCommercialApi() {
    var coverageOnly = newPluginContext();
    var coverageAndNoSonar = newPluginContext();
    try (var api = mockStatic(SonarDelphiExtensionApi.class)) {
      api.when(() -> SonarDelphiExtensionApi.integrate(any(), any()))
          .thenReturn(Set.of("COVERAGE_IMPORT"), Set.of("COVERAGE_IMPORT", "NOSONAR"));

      plugin.define(coverageOnly);
      plugin.define(coverageAndNoSonar);
    }

    assertThat(extensionKeys(coverageAndNoSonar))
        .containsExactlyElementsOf(extensionKeys(coverageOnly));
    assertThat(getExtensions(coverageAndNoSonar))
        .filteredOn(extension -> extension == DelphiNoSonarExecutor.class)
        .containsExactly(DelphiNoSonarExecutor.class);
  }

  private static Stream<Arguments> featureExtensions() {
    return Stream.of(
        Arguments.of(
            "LANGUAGE_REGISTRATION", new Object[] {Delphi.class, "sonar.delphi.file.suffixes"}),
        Arguments.of("BUILT_IN_QUALITY_PROFILE", new Object[] {DelphiSonarWayProfile.class}),
        Arguments.of("FILE_METRICS", new Object[0]),
        Arguments.of("NCLOC_DATA", new Object[0]),
        Arguments.of("DUPLICATION_TOKENS", new Object[] {DelphiCpdExecutor.class}),
        Arguments.of("SYNTAX_HIGHLIGHTING", new Object[] {DelphiHighlightExecutor.class}),
        Arguments.of("SYMBOL_PUBLICATION", new Object[0]),
        Arguments.of("SONAR_RESOLVE", new Object[] {DelphiIssueResolutionExecutor.class}),
        Arguments.of(
            "COVERAGE_IMPORT",
            new Object[] {
              DelphiCoverageSensor.class,
              DelphiCoverageParserFactory.class,
              "sonar.delphi.coverage.reportPaths"
            }),
        Arguments.of(
            "TEST_EXECUTION_IMPORT",
            new Object[] {DelphiNUnitSensor.class, "sonar.delphi.nunit.reportPaths"}),
        Arguments.of(
            "TOOLCHAIN",
            new Object[] {
              "sonar.delphi.installationPath",
              "sonar.delphi.toolchain",
              "sonar.delphi.compilerVersion"
            }),
        Arguments.of(
            "PROJECT_OPTIONS",
            new Object[] {
              "sonar.delphi.searchPath",
              "sonar.delphi.codePage",
              "sonar.delphi.conditionalDefines",
              "sonar.delphi.conditionalUndefines",
              "sonar.delphi.unitScopeNames",
              "sonar.delphi.unitAliases"
            }));
  }

  @Test
  void testMetricsExecutorIsOmittedWhenBothMetricsFeaturesAreDisabled() {
    var standalone = newPluginContext();
    plugin.define(standalone);
    var integrated = newPluginContext();
    try (var api = mockStatic(SonarDelphiExtensionApi.class)) {
      api.when(() -> SonarDelphiExtensionApi.integrate(any(), any()))
          .thenReturn(Set.of("FILE_METRICS", "NCLOC_DATA"));

      plugin.define(integrated);
    }

    assertThat(getExtensions(standalone)).contains(DelphiMetricsExecutor.class);
    assertThat(extensionKeys(integrated))
        .containsExactlyElementsOf(
            extensionKeys(standalone).stream()
                .filter(extension -> extension != DelphiMetricsExecutor.class)
                .collect(Collectors.toList()));
  }

  @Test
  void testUnknownFeatureStopsRegistration() {
    var context = newPluginContext();
    try (var api = mockStatic(SonarDelphiExtensionApi.class)) {
      api.when(() -> SonarDelphiExtensionApi.integrate(any(), any())).thenReturn(Set.of("UNKNOWN"));

      assertThatThrownBy(() -> plugin.define(context))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Unknown Sonar Delphi feature: UNKNOWN");
      assertThat(getExtensions(context)).isEmpty();
    }
  }

  @Test
  void testNullIntegrationResultStopsRegistration() {
    var context = newPluginContext();
    try (var api = mockStatic(SonarDelphiExtensionApi.class)) {
      api.when(() -> SonarDelphiExtensionApi.integrate(any(), any())).thenReturn(null);

      assertThatThrownBy(() -> plugin.define(context))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("The Sonar Delphi integration API returned an invalid result");
      assertThat(getExtensions(context)).isEmpty();
    }
  }

  @Test
  void testNonStringFeatureStopsRegistration() {
    var context = newPluginContext();
    try (var api = mockStatic(SonarDelphiExtensionApi.class)) {
      api.when(() -> SonarDelphiExtensionApi.integrate(any(), any()))
          .thenAnswer(invocation -> Set.of(1));

      assertThatThrownBy(() -> plugin.define(context))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("The Sonar Delphi integration API returned a non-string feature");
      assertThat(getExtensions(context)).isEmpty();
    }
  }

  @Test
  void testCommunityRepositoryDefinesParsingErrorRule() {
    var context = new RulesDefinition.Context();
    var metadataPath = new MetadataResourcePathImpl();
    new DelphiRulesDefinition(
            newPluginContext().getRuntime(),
            metadataPath,
            new DelphiSonarWayResourcePath(metadataPath))
        .define(context);
    assertThat(context.repository(CheckList.REPOSITORY_KEY).rule("ParsingError")).isNotNull();
  }

  @Test
  void testIntegrationFailureStopsRegistration() {
    var context = newPluginContext();
    var failure = new IllegalArgumentException("Invalid contribution");
    try (var api = mockStatic(SonarDelphiExtensionApi.class)) {
      api.when(() -> SonarDelphiExtensionApi.integrate(any(), any())).thenThrow(failure);

      assertThatThrownBy(() -> plugin.define(context))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("The Sonar Delphi integration API rejected the integration")
          .hasCause(failure);
      assertThat(getExtensions(context)).isEmpty();
    }
  }

  private static Plugin.Context newPluginContext() {
    return new Plugin.Context(
        SonarRuntimeImpl.forSonarQube(
            DelphiPlugin.ISSUE_RESOLUTION_MIN_API_VERSION,
            SonarQubeSide.SCANNER,
            SonarEdition.COMMUNITY));
  }

  private static List<Object> extensionKeys(Plugin.Context context) {
    return getExtensions(context).stream()
        .map(
            extension -> {
              if (extension instanceof PropertyDefinition) {
                return ((PropertyDefinition) extension).key();
              }
              return extension instanceof Class<?> ? extension : extension.getClass();
            })
        .collect(Collectors.toList());
  }

  private static List<Object> getExtensions(Plugin.Context context) {
    @SuppressWarnings("unchecked")
    List<Object> extensions = context.getExtensions();
    return extensions;
  }
}
