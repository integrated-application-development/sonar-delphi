# ![SonarDelphi](docs/images/sonar-delphi-title-gh.png)

[![format](https://github.com/integrated-application-development/sonar-delphi/actions/workflows/format.yml/badge.svg?branch=master&event=push)](https://github.com/integrated-application-development/sonar-delphi/actions/workflows/format.yml)
[![build](https://github.com/integrated-application-development/sonar-delphi/actions/workflows/build.yml/badge.svg?branch=master&event=push)](https://github.com/integrated-application-development/sonar-delphi/actions/workflows/build.yml)

[SonarQube](https://www.sonarqube.org) is an open platform to manage code quality. This plugin adds
Delphi support to
SonarQube.

The project was originally a [Sabre Airline Solutions](https://www.sabre.com) donation, and has been
iterated on by
various open-source maintainers in the intervening years. In 2018, it was revived as a Monash
University student
project for [IntegraDev](https://www.integradev.com.au).

Since 2019, the project has been maintained by IntegraDev.

Features
-------

* 100+ rules
* Metrics (complexity, number of lines, etc.)
* Import of test coverage reports (compatible
  with [DelphiCodeCoverage](https://sourceforge.net/p/delphicodecoverage/git/ci/master/tree/))
* Custom rules

Quickstart
----------

Follow these steps to get started:

1. **Prerequisites:**
    - [SonarQube](https://docs.sonarqube.org/latest/setup/install-server/) (v9.9+)
    - [SonarScanner](https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/scanners/sonarscanner/)
    - [Delphi](https://www.embarcadero.com/products/delphi)

2. **Install Plugin:**
    - Download the SonarDelphi plugin
      from [Releases](https://github.com/integrated-application-development/sonar-delphi/releases).
    - [Install the plugin](https://docs.sonarqube.org/latest/setup/install-plugin/).

3. **Configure Analysis:**
    - Configure [language-specific properties](#language-specific-properties).

4. **Run Analysis:**
    - Execute `sonar-scanner` in your project's directory.

5. **View Results:**
    - Visit the link provided at the end of the scan to view analysis results on SonarQube.

>
> :warning: **Note**
>
> The SonarDelphi analyzer requires source code for all dependencies, including the standard
> library.
>
> As a result, Delphi CE is not supported.
>

## Language-specific properties

You can discover and update the
Delphi-specific [properties](https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/analysis-parameters/)
in: **Administration > General Settings > Languages > Delphi**

By default, all `__history` and `__recovery` directories are excluded from the analysis.
However, you can change the property `sonar.delphi.exclusions` to a different pattern if you want to
force their analysis (not recommended).

| Key                                 | Value                                                                                                                                                                                                                                                  | Default Value                                    |
|-------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------|
| `sonar.delphi.file.suffixes`        | List of suffixes for Delphi files to analyze. To not filter, leave the list empty.                                                                                                                                                                     | `.pas,.dpr,.dpk`                                 |
| `sonar.delphi.exclusions`           | List of file path patterns to be excluded from analysis of Delphi files.                                                                                                                                                                               | `**/__history/**,**/__recovery/**`               |
| `sonar.delphi.installationPath`     | Path to the Delphi installation folder.<br/><br/>:warning: **Note**: This must point to a valid Delphi IDE installation, or the scan will fail.                                                                                                        | `C:\Program Files (x86)\Embarcadero\Studio\22.0` |
| `sonar.delphi.toolchain`            | The compiler toolchain.<br/>Options: `DCC32`, `DCC64`, `DCCOSX`, `DCCOSX64`, `DCCIOSARM`, `DCCIOSARM64`, `DCCIOS32`, `DCCAARM`, `DCCAARM64`, `DCCLINUX64`<br/>See: [Delphi Toolchains](https://docwiki.embarcadero.com/RADStudio/en/Delphi_Toolchains) | `DCC32`                                          |
| `sonar.delphi.compilerVersion`      | The Delphi conditional symbol representing the compiler version.<br/>Format: `VER<nnn>`.<br/>See: [Compiler Versions](http://docwiki.embarcadero.com/RADStudio/en/Compiler_Versions)                                                                   | `VER350`                                         |
| `sonar.delphi.searchPath`           | List of directories to search for include files and unit imports. Each path may be absolute or relative to the project base directory                                                                                                                  | -                                                |
| `sonar.delphi.conditionalDefines`   | List of conditional defines to define while parsing the project, in addition to the defines aggregated from the project files                                                                                                                          | -                                                |
| `sonar.delphi.conditionalUndefines` | List of conditional defines to consider undefined while parsing the project. This is useful for flicking off some specific defines that were aggregated from the project files.                                                                        | -                                                |
| `sonar.delphi.unitScopeNames`       | List of unit scope names, used for import resolution.                                                                                                                                                                                                  | -                                                |
| `sonar.delphi.unitAliases`          | List of unit aliases, used for import resolution.<br/>Format: `AliasName=UnitName`                                                                                                                                                                     | -                                                |
| `sonar.delphi.testType`             | A fully qualified type name. Any code within this type or its descendants will be treated as test code                                                                                                                                                 | `TestFramework.TTestCase`                        |
| `sonar.delphi.nunit.reportPaths`    | List of directories containing the `*.xml` NUnit report files. Each path may be absolute or relative to the project base directory.                                                                                                                    | -                                                |
| `sonar.delphi.coverage.reportPaths` | List of directories containing the `*.xml` Delphi Code Coverage report files. Each path may be absolute or relative to the project base directory.                                                                                                     | -                                                |

Development
-----------

SonarDelphi targets Java 11 and can be built with JDK 17+.

To build and run tests, execute the following command from the project's root directory:

```
mvn clean install
```

License
-------

Licensed under
the [GNU Lesser General Public License, Version 3.0](http://www.gnu.org/licenses/lgpl.txt).
