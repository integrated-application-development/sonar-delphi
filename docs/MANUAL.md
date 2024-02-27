# SonarDelphi Manual

- [SonarDelphi Manual](#sonardelphi-manual)
  - [SonarQube from first principles](#sonarqube-from-first-principles)
  - [Configuring a SonarDelphi project](#configuring-a-sonardelphi-project)
    - [Scoping Sonar projects](#scoping-sonar-projects)
    - [Configuring Sonar projects](#configuring-sonar-projects)
    - [Advanced project configuration](#advanced-project-configuration)
  - [Run a SonarDelphi scan](#run-a-sonardelphi-scan)
  - [Configure analysis rules](#configure-analysis-rules)
    - [Custom rules](#custom-rules)
      - [Template rules](#template-rules)
    - [Custom rules plugins](#custom-rules-plugins)

## SonarQube from first principles

SonarDelphi is a plugin that adds Delphi language support to the Sonar code analysis platform.

Sonar has two main components:

* A **server** (SonarQube):
  * Stores project scan results ("issues" and "metrics")
  * Has a web UI that can be used to browse issues detected in source files
  * Allows users to change issue statuses, add comments to issues, etc.
  * Supports various plugins that provide functionality for the server, the scanner, or both
* A **scanner** (SonarScanner):
  * "Scans" a project locally by downloading plugins from the server and running them on the local files
  * Each plugin:
    1. Reads the source code files that are relevant to it
    2. Calculates **metrics** (e.g. lines of code, coverage, etc.)
    3. Analyzes the source code, detecting **issues** by applying **rules**
  * At the end of a scan, the scanner sends the metrics and issues to the server

This is a very high-level description - for more details, please refer to the
[SonarQube documentation](https://docs.sonarsource.com/sonarqube/latest/).

As a language plugin, SonarDelphi's job is to:

1. Read and understand Delphi code
2. Calculate metrics in Delphi code
3. Detect issues in Delphi code

## Configuring a SonarDelphi project

### Scoping Sonar projects

The first step is to identify how your Delphi codebase maps to individual Sonar projects.

SonarDelphi reads configuration information from project files that are included in analysis,
including:

* Unit search path (for import resolution)
* Compiler defines (for parsing source files)
* Unit scope names (to allow unqualified imports like `SysUtils` instead of `System.SysUtils`)
* Unit aliases (to resolve alternative import names like `Actions` to `ActnList`)

For this information to be accurate, a Sonar project should ideally contain only one Delphi
project file (`.dproj`).

For more complex cases in which this is not feasible, refer to [Project Scoping](PROJECT_SCOPING.md).

### Configuring Sonar projects

Once you've determined how your Sonar projects will be organised, add a `sonar-project.properties` for
each project. The scanner looks for this configuration file in the current working directory when
invoked.

The most important general settings in a `sonar-project.properties` can be seen below.

```ini
# The key of the corresponding project on SonarQube.
# Defaults to the name of the containing directory.
sonar.projectKey=my-project

# By default, Sonar takes the directory containing sonar-project.properties as
# the analysis root directory.
#
# This property lets you configure it to start in another directory.
# This is useful if you have multiple Sonar projects that should have the
# same analysis root.
sonar.projectBaseDir=.

# Comma-separated list of source files.
# If you provide a directory, all files within that
# directory will be included (except those excluded by sonar.exclusions).
#
# Delphi files in sonar.sources will be analysed and have issues generated -
# they are considered "main code" for the purposes of issue detection.
# SonarDelphi can also read Delphi files that are on the search path
# (read from sonar.delphi.searchPath and .dproj files), but issues
# will not be detected in those files.
sonar.sources=source,packages/mylib.dpk,packages/mylib.dproj

# Comma-separated list of test files.
# If you provide a directory, all files within that
# directory will be included (except those excluded by sonar.exclusions).
#
# Delphi files in sonar.tests will be analysed and have issues generated -
# they are considered "test code" for the purposes of issue detection.
sonar.tests=test

# The text encoding to use when reading source files.
# Delphi files are typically either ANSI (windows-1252, etc.) or
# UTF-8 with BOM (utf-8).
sonar.sourceEncoding=utf-8

# Comma-separated list of file patterns to exclude from analysis.
sonar.exclusions=**/target/**
```

SonarDelphi also provides a number of configuration options, the most important of which
are mentioned below. See [Language-specific properties](CONFIGURATION.md#language-specific-properties)
for an exhaustive list.

```ini
# Absolute path to the Delphi installation to use.
sonar.delphi.installationPath=C:\Program Files (x86)\Embarcadero\Studio\23.0

# The compiler symbol relating to the Delphi version being targeted.
sonar.delphi.compilerVersion=VER360

# The Delphi compiler being targeted.
sonar.delphi.toolchain=DCC64
```

> [!IMPORTANT]
> `sonar.delphi.installationPath` must point to a valid Delphi installation for the scan to succeed.

An improperly configured `sonar-project.properties` is the most common cause of analysis issues and
inconsistencies when using SonarDelphi - if you get it right, there's not much SonarDelphi can't handle.

### Advanced project configuration

SonarQube exposes configuration for code duplication thresholds, which can be useful to adjust for
verbose languages like Delphi.

```ini
# By default, a piece of code is considered duplicated as soon as there are at least
# 100 successive and duplicated tokens spread over at least 10 lines of code.
sonar.cpd.delphi.minimumTokens=200
sonar.cpd.delphi.minimumLines=20
```

For more details on SonarQube configuration options, see
the [SonarQube documentation](https://docs.sonarsource.com/sonarcloud/advanced-setup/analysis-parameters/).

In addition to `sonar.tests`, SonarDelphi allows you to configure certain types and their methods as test code.
This is designed for compatibility with common Delphi test frameworks and can be configured in two ways:

```ini
# Code within this type and all descendants is considered test code.
# Defaults to DUnit's test base type: TestFramework.TTestCase.
sonar.delphi.testType=TestFramework.TTestCase

# Code within any type annotated with this attribute is considered test code.
# Defaults to DUnitX's test attribute: DUnitX.Attributes.TestFixtureAttribute.
sonar.delphi.testAttribute=DUnitX.Attributes.TestFixtureAttribute
```

For the full list of configuration options, see [Configuring SonarDelphi](CONFIGURATION.md).

## Run a SonarDelphi scan

Once your project is configured, you simply
[run the scanner](https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/scanners/sonarscanner/)
in the same directory as your `sonar-project.properties`.

After an analysis, the project page on SonarQube will refresh with updated issues and metrics.
Browsing the issues, you have the ability to update them if needed - assign them to other users, add comments,
or change their status (e.g. accepting issues you won't fix or hiding false positives).

If there are a large number of issues for rules that don't suit your codebase - for example, issues complaining
your classes don't begin with `T` when you have a different convention - you may benefit from
[customizing analysis rules](#configure-analysis-rules).

## Configure analysis rules

The rules applied during a scan are dictated by the project's
[quality profile](https://docs.sonarsource.com/sonarqube/latest/instance-administration/quality-profiles/).

For Delphi, the default `Sonar way` quality profile contains sensible default rules that are generally
accepted to be best practice. These may not be helpful to apply to your codebase if it has its own conventions that
conflict with the rules.

Some common quality profile changes include:
* Setting `excludeApi` to `true` (allowing "unused" public API) on `UnusedConstant`, `UnusedField`,
  `UnusedGlobalVariable`, `UnusedProperty`, `UnusedRoutine`, and `UnusedType`
* Changing the `prefixes` on `ClassName`, `RecordName`, `UnitName`, `EnumName`, `InterfaceName`,
  and `HelperName`
* Disabling `ConstantName`
* Disabling `ShortIdentifier`
* Disabling `NilComparison`

SonarDelphi also provides many non-default rules that are more niche or opinionated.
Some rules that are often added to quality profiles include:

* `BeginEndRequired` ('begin'..'end' should always be used)
* `WithStatement` ('with' statements should not be used)
* `GroupedFieldDeclaration` (Fields should be declared individually)
* `InterfaceGuid` (Interfaces should have unique GUIDs)
* `CatchingRawException` (Raw exceptions should not be caught)
* `DateFormatSettings` (Date formatting should not use the default TFormatSettings)

Using quality profiles allows you to track the quality of your code as *you* define it.

### Custom rules

#### Template rules

For additional customization, SonarDelphi provides a number of
[template rules](https://docs.sonarsource.com/sonarqube/latest/user-guide/rules/overview/#rule-templates-and-custom-rules).
These templates can be used to create your own custom rules entirely in the UI, with no coding required.

### Custom rules plugins

For even **more** customization, SonarDelphi supports "custom rules" plugins - SonarQube plugins that
provide additional rules to the core analyzer. These rules, written in Java, have the same API as the
core rules and are capable of all the same functionality.

For more details, see [Writing Custom Delphi Rules](CUSTOM_RULES.md).