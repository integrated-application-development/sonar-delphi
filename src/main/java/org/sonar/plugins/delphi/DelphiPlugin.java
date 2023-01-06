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
package org.sonar.plugins.delphi;

import com.google.common.collect.ImmutableList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.plugins.delphi.compiler.CompilerVersion;
import org.sonar.plugins.delphi.compiler.Toolchain;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.coverage.DelphiCoverageParserFactory;
import org.sonar.plugins.delphi.coverage.DelphiCoverageSensor;
import org.sonar.plugins.delphi.coverage.delphicodecoveragetool.DelphiCodeCoverageToolParser;
import org.sonar.plugins.delphi.enviroment.DefaultEnvironmentVariableProvider;
import org.sonar.plugins.delphi.executor.DelphiCpdExecutor;
import org.sonar.plugins.delphi.executor.DelphiHighlightExecutor;
import org.sonar.plugins.delphi.executor.DelphiMasterExecutor;
import org.sonar.plugins.delphi.executor.DelphiMetricsExecutor;
import org.sonar.plugins.delphi.executor.DelphiPmdExecutor;
import org.sonar.plugins.delphi.executor.DelphiSymbolTableExecutor;
import org.sonar.plugins.delphi.msbuild.DelphiProjectHelper;
import org.sonar.plugins.delphi.nunit.DelphiNUnitSensor;
import org.sonar.plugins.delphi.pmd.DelphiPmdConfiguration;
import org.sonar.plugins.delphi.pmd.profile.DefaultDelphiProfile;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdProfileExporter;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdProfileImporter;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdRuleSetDefinitionProvider;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdRulesDefinition;
import org.sonar.plugins.delphi.pmd.violation.DelphiPmdViolationRecorder;

/** Main Sonar DelphiLanguage plugin class */
public class DelphiPlugin implements Plugin {
  public static final String SEARCH_PATH_KEY = "sonar.delphi.sources.searchPath";
  public static final String BDS_PATH_KEY = "sonar.delphi.bds.path";
  public static final String COMPILER_TOOLCHAIN_KEY = "sonar.delphi.compiler.toolchain";
  public static final String COMPILER_VERSION_KEY = "sonar.delphi.compiler.version";
  public static final String CONDITIONAL_DEFINES_KEY = "sonar.delphi.conditionalDefines";
  public static final String CONDITIONAL_UNDEFINES_KEY = "sonar.delphi.conditionalUndefines";
  public static final String UNIT_SCOPE_NAMES_KEY = "sonar.delphi.unitScopeNames";
  public static final String UNIT_ALIASES_KEY = "sonar.delphi.unitAliases";
  public static final String COVERAGE_TOOL_KEY = "sonar.delphi.coverage.tool";
  public static final String COVERAGE_REPORT_KEY = "sonar.delphi.coverage.reportPaths";
  public static final String NUNIT_REPORT_PATHS_PROPERTY = "sonar.delphi.nunit.reportPaths";
  public static final String GENERATE_PMD_REPORT_XML_KEY = "sonar.delphi.pmd.generateXml";
  public static final String TEST_SUITE_TYPE_KEY = "sonar.delphi.pmd.testSuiteType";

  public static final Toolchain COMPILER_TOOLCHAIN_DEFAULT = Toolchain.DCC32;
  public static final CompilerVersion COMPILER_VERSION_DEFAULT =
      CompilerVersion.fromVersionSymbol("VER340");

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  @Override
  public void define(Context context) {
    ImmutableList.Builder<Object> builder = ImmutableList.builder();

    builder.add(
        PropertyDefinition.builder(DelphiPlugin.SEARCH_PATH_KEY)
            .name("Search path")
            .description("Directories to search in for include files and unit imports.")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiPlugin.BDS_PATH_KEY)
            .name("BDS path")
            .description(
                "Path to the Delphi BDS folder."
                    + "Example: C:\\Program Files (x86)\\Embarcadero\\Studio\\20.0")
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiPlugin.COMPILER_TOOLCHAIN_KEY)
            .name("Compiler toolchain")
            .defaultValue(COMPILER_TOOLCHAIN_DEFAULT.name())
            .description(
                "The compiler toolchain used by this project. Options are: "
                    + StringUtils.join(
                        Stream.of(Toolchain.values())
                            .map(value -> "\"" + value + "\"")
                            .collect(Collectors.toList())))
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiPlugin.COMPILER_VERSION_KEY)
            .name("Compiler version")
            .defaultValue(COMPILER_VERSION_DEFAULT.symbol())
            .description(
                "The Delphi conditional symbol representing the compiler version."
                    + " Format is \"VER&lt;nnn&gt;\"."
                    + " See: http://docwiki.embarcadero.com/RADStudio/en/Compiler_Versions")
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiPlugin.CONDITIONAL_DEFINES_KEY)
            .name("Conditional Defines")
            .description("List of conditional defines to define while parsing the project")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiPlugin.CONDITIONAL_UNDEFINES_KEY)
            .name("Conditional Undefines")
            .description(
                "List of conditional defines to undefine before parsing the project. This is useful"
                    + " if you aggregate the defines from your project files, but still want to"
                    + " exclude certain ones.")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiPlugin.UNIT_SCOPE_NAMES_KEY)
            .name("Unit Scope Names")
            .description("List of Unit scope names, used for unit import resolution.")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiPlugin.UNIT_ALIASES_KEY)
            .name("Unit Aliases")
            .description(
                "List of Unit Aliases, used for unit import resolution."
                    + " NOTE: Each Unit Alias should follow this format: 'AliasName=UnitName'")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiPlugin.COVERAGE_TOOL_KEY)
            .name("Coverage Tool")
            .defaultValue(DelphiCodeCoverageToolParser.KEY)
            .description(
                "Used coverage tool. Options are: \"" + DelphiCodeCoverageToolParser.KEY + "\"")
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiPlugin.COVERAGE_REPORT_KEY)
            .name("Coverage Report Paths")
            .description("List of paths containing coverage reports from the specified tool")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiPlugin.NUNIT_REPORT_PATHS_PROPERTY)
            .name("NUnit Report Paths")
            .description("List of paths to NUnit report directories")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiPlugin.GENERATE_PMD_REPORT_XML_KEY)
            .name("Generate XML Report")
            .defaultValue("false")
            .description("Whether a PMD Report XML file should be generated")
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiPlugin.TEST_SUITE_TYPE_KEY)
            .name("Test Suite Type")
            .defaultValue("")
            .description(
                "Rules can be configured not to apply to test code. Any type that inherits from the"
                    + " Test Suite type will have its methods considered as test code."
                    + " NOTE: A fully qualified type name is expected.")
            .onQualifiers(Qualifiers.PROJECT)
            .build());

    builder.add(
        // Sensors
        DelphiSensor.class,
        DelphiCoverageSensor.class,
        DelphiNUnitSensor.class,
        // Executors
        DelphiMasterExecutor.class,
        DelphiCpdExecutor.class,
        DelphiHighlightExecutor.class,
        DelphiMetricsExecutor.class,
        DelphiSymbolTableExecutor.class,
        DelphiPmdExecutor.class,
        // Core
        DelphiLanguage.class,
        // Core helpers
        DelphiProjectHelper.class,
        DelphiCoverageParserFactory.class,
        // PMD
        DelphiPmdConfiguration.class,
        DelphiPmdRulesDefinition.class,
        DelphiPmdRuleSetDefinitionProvider.class,
        DefaultDelphiProfile.class,
        DelphiPmdProfileExporter.class,
        DelphiPmdProfileImporter.class,
        DelphiPmdViolationRecorder.class,
        // Environment
        DefaultEnvironmentVariableProvider.class);

    context.addExtensions(builder.build());
  }
}
