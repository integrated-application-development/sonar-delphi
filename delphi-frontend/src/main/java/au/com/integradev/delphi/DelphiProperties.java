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

import au.com.integradev.delphi.compiler.CompilerVersion;
import au.com.integradev.delphi.compiler.Toolchain;
import au.com.integradev.delphi.core.Delphi;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

public final class DelphiProperties {
  public static final String EXCLUSIONS_KEY = "sonar.delphi.exclusions";
  public static final String INSTALLATION_PATH_KEY = "sonar.delphi.installationPath";
  public static final String COMPILER_TOOLCHAIN_KEY = "sonar.delphi.toolchain";
  public static final String COMPILER_VERSION_KEY = "sonar.delphi.compilerVersion";
  public static final String SEARCH_PATH_KEY = "sonar.delphi.searchPath";
  public static final String CONDITIONAL_DEFINES_KEY = "sonar.delphi.conditionalDefines";
  public static final String CONDITIONAL_UNDEFINES_KEY = "sonar.delphi.conditionalUndefines";
  public static final String UNIT_SCOPE_NAMES_KEY = "sonar.delphi.unitScopeNames";
  public static final String UNIT_ALIASES_KEY = "sonar.delphi.unitAliases";
  public static final String TEST_TYPE_KEY = "sonar.delphi.testType";
  public static final String TEST_ATTRIBUTE_KEY = "sonar.delphi.testAttribute";
  public static final String NUNIT_REPORT_PATHS_PROPERTY = "sonar.delphi.nunit.reportPaths";
  public static final String COVERAGE_REPORT_KEY = "sonar.delphi.coverage.reportPaths";

  private static final String DELPHI_CATEGORY = "Delphi";
  private static final String GENERAL_SUBCATEGORY = "General";
  private static final String TOOLCHAIN_SUBCATEGORY = "Toolchain";
  private static final String PROJECT_OPTIONS_SUBCATEGORY = "Project Options";
  private static final String TEST_SUBCATEGORY = "Test and Coverage";

  public static final String EXCLUSIONS_DEFAULT_VALUE = "**/__history/**,**/__recovery/**";
  private static final String INSTALLATION_PATH_DEFAULT =
      "C:\\Program Files (x86)\\Embarcadero\\Studio\\22.0";
  public static final Toolchain COMPILER_TOOLCHAIN_DEFAULT = Toolchain.DCC32;
  public static final CompilerVersion COMPILER_VERSION_DEFAULT =
      CompilerVersion.fromVersionSymbol("VER350");
  private static final String TEST_TYPE_DEFAULT = "TestFramework.TTestCase";
  private static final String TEST_ATTRIBUTE_DEFAULT = "DUnitX.Attributes.TestFixtureAttribute";

  private DelphiProperties() {
    // hide public constructor
  }

  public static List<PropertyDefinition> getProperties() {
    return List.of(
        PropertyDefinition.builder(Delphi.FILE_SUFFIXES_KEY)
            .category(DELPHI_CATEGORY)
            .subCategory(GENERAL_SUBCATEGORY)
            .defaultValue(Delphi.DEFAULT_FILE_SUFFIXES)
            .name("File suffixes")
            .description(
                "List of suffixes for Delphi files to analyze."
                    + " To not filter, leave the list empty.")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(EXCLUSIONS_KEY)
            .category(DELPHI_CATEGORY)
            .subCategory(GENERAL_SUBCATEGORY)
            .defaultValue(EXCLUSIONS_DEFAULT_VALUE)
            .name("Delphi exclusions")
            .description("List of file path patterns to be excluded from analysis of Delphi files.")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.INSTALLATION_PATH_KEY)
            .category(DELPHI_CATEGORY)
            .subCategory(TOOLCHAIN_SUBCATEGORY)
            .defaultValue(INSTALLATION_PATH_DEFAULT)
            .name("Delphi installation path")
            .description("Path to the Delphi installation folder.")
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.COMPILER_TOOLCHAIN_KEY)
            .category(DELPHI_CATEGORY)
            .subCategory(TOOLCHAIN_SUBCATEGORY)
            .defaultValue(COMPILER_TOOLCHAIN_DEFAULT.name())
            .name("Compiler toolchain")
            .description(
                "The compiler toolchain. Options: "
                    + StringUtils.join(
                        Stream.of(Toolchain.values())
                            .map(value -> "`" + value + "`")
                            .collect(Collectors.toList())))
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.COMPILER_VERSION_KEY)
            .category(DELPHI_CATEGORY)
            .subCategory(TOOLCHAIN_SUBCATEGORY)
            .defaultValue(COMPILER_VERSION_DEFAULT.symbol())
            .name("Compiler version")
            .description(
                "The Delphi conditional symbol representing the compiler version."
                    + " Format: `VER&lt;nnn&gt;`.")
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.SEARCH_PATH_KEY)
            .category(DELPHI_CATEGORY)
            .subCategory(PROJECT_OPTIONS_SUBCATEGORY)
            .name("Search path")
            .description(
                "List of directories to search for include files and unit imports."
                    + " Each path may be absolute or relative to the project base directory.")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.CONDITIONAL_DEFINES_KEY)
            .category(DELPHI_CATEGORY)
            .subCategory(PROJECT_OPTIONS_SUBCATEGORY)
            .name("Conditional defines")
            .description(
                "List of conditional defines to define while parsing the project, in addition to"
                    + " the defines aggregated from the project files.")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.CONDITIONAL_UNDEFINES_KEY)
            .category(DELPHI_CATEGORY)
            .subCategory(PROJECT_OPTIONS_SUBCATEGORY)
            .name("Conditional undefines")
            .description(
                "List of conditional defines to consider undefined while parsing the project."
                    + " This is useful for flicking off some specific defines that were aggregated"
                    + " from the project files.")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.UNIT_SCOPE_NAMES_KEY)
            .category(DELPHI_CATEGORY)
            .subCategory(PROJECT_OPTIONS_SUBCATEGORY)
            .name("Unit scope names")
            .description("List of unit scope names, used for import resolution.")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.UNIT_ALIASES_KEY)
            .category(DELPHI_CATEGORY)
            .subCategory(PROJECT_OPTIONS_SUBCATEGORY)
            .name("Unit aliases")
            .description(
                "List of unit aliases, used for import resolution. Format: `AliasName=UnitName`")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.TEST_TYPE_KEY)
            .category(DELPHI_CATEGORY)
            .subCategory(TEST_SUBCATEGORY)
            .defaultValue(TEST_TYPE_DEFAULT)
            .name("Test Type")
            .description(
                "A fully qualified type name. Any code within this type or its descendants will be"
                    + " treated as test code.")
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.TEST_ATTRIBUTE_KEY)
            .category(DELPHI_CATEGORY)
            .subCategory(TEST_SUBCATEGORY)
            .defaultValue(TEST_ATTRIBUTE_DEFAULT)
            .name("Test Attribute")
            .description(
                "A fully qualified type name. Any code within a type that is annotated"
                    + " with this attribute will be treated as test code.")
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.NUNIT_REPORT_PATHS_PROPERTY)
            .category(DELPHI_CATEGORY)
            .subCategory(TEST_SUBCATEGORY)
            .name("Path to NUnit report(s)")
            .description(
                "List of directories containing the *.xml NUnit report files."
                    + " Each path may be absolute or relative to the project base directory.")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.COVERAGE_REPORT_KEY)
            .category(DELPHI_CATEGORY)
            .subCategory(TEST_SUBCATEGORY)
            .name("Path to coverage report(s)")
            .description(
                "List of directories containing the *.xml Delphi Code Coverage report files."
                    + " Each path may be absolute or relative to the project base directory")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build());
  }
}
