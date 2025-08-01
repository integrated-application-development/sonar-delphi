# Configuring SonarDelphi

SonarDelphi exposes a number of language-specific properties as [analysis parameters](https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/analysis-parameters/),
which can be:
* configured via the SonarQube UI (**Administration > General Settings > Delphi**)
* specified in `sonar-project.properties`
* passed as command-line parameters to the `sonar-scanner`

By default, all `__history` and `__recovery` directories are excluded from the analysis.
However, you can change the property `sonar.delphi.exclusions` to a different pattern if you want to
force their analysis (not recommended).

## Language-specific properties

### General

| Key                          | Value                                                                              | Default Value                      |
|------------------------------|------------------------------------------------------------------------------------|------------------------------------|
| `sonar.delphi.file.suffixes` | List of suffixes for Delphi files to analyze. To not filter, leave the list empty. | `.pas,.dpr,.dpk`                   |
| `sonar.delphi.exclusions`    | List of file path patterns to be excluded from analysis of Delphi files.           | `**/__history/**,**/__recovery/**` |

### Toolchain

| Key                             | Value                                                                                                                                                                                                                                                  | Default Value                                    |
|---------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------|
| `sonar.delphi.installationPath` | Path to the Delphi installation folder.<br/><br/>:warning: **Note**: This must point to a valid Delphi IDE installation for the scan to succeed.                                                                                                       | `C:\Program Files (x86)\Embarcadero\Studio\22.0` |
| `sonar.delphi.toolchain`        | The compiler toolchain.<br/>Options: `DCC32`, `DCC64`, `DCCOSX`, `DCCOSX64`, `DCCIOSARM`, `DCCIOSARM64`, `DCCIOS32`, `DCCAARM`, `DCCAARM64`, `DCCLINUX64`<br/>See: [Delphi Toolchains](https://docwiki.embarcadero.com/RADStudio/en/Delphi_Toolchains) | `DCC32`                                          |
| `sonar.delphi.compilerVersion`  | The Delphi conditional symbol representing the compiler version.<br/>Format: `VER<nnn>`.<br/>See: [Compiler Versions](https://docwiki.embarcadero.com/RADStudio/en/Compiler_Versions)                                                                  | `VER350`                                         |

### Project Options

> [!NOTE]
> Project options are indexed from project (**.dproj**) files.
>
> These properties allow you to add or remove values from those indexed options.

| Key                                 | Value                                                                                                                                  | Default Value |
|-------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|---------------|
| `sonar.delphi.searchPath`           | List of directories to search for include files and unit imports. Each path may be absolute or relative to the project base directory. | -             |
| `sonar.delphi.conditionalDefines`   | List of conditional defines to consider defined while parsing the project.                                                             | -             |
| `sonar.delphi.conditionalUndefines` | List of conditional defines to consider undefined while parsing the project.                                                           | -             |
| `sonar.delphi.unitScopeNames`       | List of unit scope names, used for import resolution.                                                                                  | -             |
| `sonar.delphi.unitAliases`          | List of unit aliases, used for import resolution.<br/>Format: `AliasName=UnitName`                                                     | -             |

### Test and Coverage

| Key                                 | Value                                                                                                                                              | Default Value                            |
|-------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------|
| `sonar.delphi.testAttribute`        | A fully qualified type name. Any code within a type that is annotated with this attribute will be treated as test code.                            | `DUnitX.Attributes.TestFixtureAttribute` |
| `sonar.delphi.testType`             | A fully qualified type name. Any code within this type or its descendants will be treated as test code.                                            | `TestFramework.TTestCase`                |
| `sonar.delphi.nunit.reportPaths`    | List of directories containing the `*.xml` NUnit report files. Each path may be absolute or relative to the project base directory.                | -                                        |
| `sonar.delphi.coverage.reportPaths` | List of directories containing the `*.xml` Delphi Code Coverage report files. Each path may be absolute or relative to the project base directory. | -                                        |