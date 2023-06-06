package au.com.integradev.delphi;

import au.com.integradev.delphi.compiler.CompilerVersion;
import au.com.integradev.delphi.compiler.Toolchain;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

public final class DelphiProperties {
  public static final Toolchain COMPILER_TOOLCHAIN_DEFAULT = Toolchain.DCC32;
  public static final CompilerVersion COMPILER_VERSION_DEFAULT =
      CompilerVersion.fromVersionSymbol("VER340");

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
  public static final String TEST_SUITE_TYPE_KEY = "sonar.delphi.testSuiteType";

  public static final String COVERAGE_TOOL_DELPHI_CODE_COVERAGE = "dcc";

  private DelphiProperties() {
    // hide public constructor
  }

  public static List<PropertyDefinition> getProperties() {
    return List.of(
        PropertyDefinition.builder(DelphiProperties.SEARCH_PATH_KEY)
            .name("Search path")
            .description("Directories to search in for include files and unit imports.")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.BDS_PATH_KEY)
            .name("BDS path")
            .description(
                "Path to the Delphi BDS folder."
                    + "Example: C:\\Program Files (x86)\\Embarcadero\\Studio\\20.0")
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.COMPILER_TOOLCHAIN_KEY)
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
        PropertyDefinition.builder(DelphiProperties.COMPILER_VERSION_KEY)
            .name("Compiler version")
            .defaultValue(COMPILER_VERSION_DEFAULT.symbol())
            .description(
                "The Delphi conditional symbol representing the compiler version."
                    + " Format is \"VER&lt;nnn&gt;\"."
                    + " See: http://docwiki.embarcadero.com/RADStudio/en/Compiler_Versions")
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.CONDITIONAL_DEFINES_KEY)
            .name("Conditional Defines")
            .description("List of conditional defines to define while parsing the project")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.CONDITIONAL_UNDEFINES_KEY)
            .name("Conditional Undefines")
            .description(
                "List of conditional defines to undefine before parsing the project. This is useful"
                    + " if you aggregate the defines from your project files, but still want to"
                    + " exclude certain ones.")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.UNIT_SCOPE_NAMES_KEY)
            .name("Unit Scope Names")
            .description("List of Unit scope names, used for unit import resolution.")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.UNIT_ALIASES_KEY)
            .name("Unit Aliases")
            .description(
                "List of Unit Aliases, used for unit import resolution."
                    + " NOTE: Each Unit Alias should follow this format: 'AliasName=UnitName'")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.COVERAGE_TOOL_KEY)
            .name("Coverage Tool")
            .defaultValue(DelphiProperties.COVERAGE_TOOL_DELPHI_CODE_COVERAGE)
            .description(
                "Used coverage tool. Options are: \""
                    + DelphiProperties.COVERAGE_TOOL_DELPHI_CODE_COVERAGE
                    + "\"")
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.COVERAGE_REPORT_KEY)
            .name("Coverage Report Paths")
            .description("List of paths containing coverage reports from the specified tool")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.NUNIT_REPORT_PATHS_PROPERTY)
            .name("NUnit Report Paths")
            .description("List of paths to NUnit report directories")
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(DelphiProperties.TEST_SUITE_TYPE_KEY)
            .name("Test Suite Type")
            .defaultValue("")
            .description(
                "Rules can be configured not to apply to test code. Any type that inherits from the"
                    + " Test Suite type will have its methods considered as test code."
                    + " NOTE: A fully qualified type name is expected.")
            .onQualifiers(Qualifiers.PROJECT)
            .build());
  }
}
