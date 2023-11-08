# ![SonarDelphi](docs/images/sonar-delphi-title-gh.png)

[![format](https://github.com/integrated-application-development/sonar-delphi/actions/workflows/format.yml/badge.svg?branch=master&event=push)](https://github.com/integrated-application-development/sonar-delphi/actions/workflows/format.yml)
[![build](https://github.com/integrated-application-development/sonar-delphi/actions/workflows/build.yml/badge.svg?branch=master&event=push)](https://github.com/integrated-application-development/sonar-delphi/actions/workflows/build.yml)

SonarDelphi is a modern, performant, and fully-featured community code analyzer for the Delphi
language. As a plugin for the [SonarQube](https://www.sonarqube.org) code analysis platform, it can
be easily integrated into existing workflows.

This project has three primary goals:

* **Correctness:** All valid Delphi code should be accepted and understood correctly.
* **Utility:** Analysis results should be easily understandable and immediately actionable.
* **Extensibility:** New rules should be trivial to add and have access to the full power of the analyzer.

This project aims to follow the conventions and best practices of SonarQube's official analyzers. It is actively
maintained by a core team and is [open for community contributions](#contributing).

## Features

With SonarDelphi, you can:

* Analyze Delphi code, identifying issues from more than 120 rules
* Create custom rules in the SonarQube UI from templates
* Import [NUnit](https://docs.nunit.org/articles/nunit/technical-notes/usage/Test-Result-XML-Format.html) test reports (compatible with [DUnitX](https://github.com/VSoftTechnologies/DUnitX))
* Import test coverage reports (compatible with [DelphiCodeCoverage](https://github.com/DelphiCodeCoverage/DelphiCodeCoverage))

### Semantic analysis

SonarDelphi understands what your code *means*, paving the way for powerful rules such as:
   * [Variables must be initialized before being used](delphi-checks/src/main/java/au/com/integradev/delphi/checks/VariableInitializationCheck.java)
   * [Name casing should be kept consistent](delphi-checks/src/main/java/au/com/integradev/delphi/checks/MixedNamesCheck.java)
   * [FreeAndNil must only be passed an instance of a TObject descendant](delphi-checks/src/main/java/au/com/integradev/delphi/checks/FreeAndNilTObjectCheck.java)
   * [Constructors should not be invoked on object variables](delphi-checks/src/main/java/au/com/integradev/delphi/checks/InstanceInvokedConstructorCheck.java)
   * [The Single overloads of the standard math functions should not be used](delphi-checks/src/main/java/au/com/integradev/delphi/checks/MathFunctionSingleOverloadCheck.java)
   * [Redundant casts should not be used](delphi-checks/src/main/java/au/com/integradev/delphi/checks/RedundantCastCheck.java)
   * [Platform-dependent casts should not be used](delphi-checks/src/main/java/au/com/integradev/delphi/checks/PlatformDependentCastCheck.java)
   * [Unicode types should not be cast to ANSI types](delphi-checks/src/main/java/au/com/integradev/delphi/checks/UnicodeToAnsiCastCheck.java)
   * Your own custom rules to [enforce a naming convention for descendants of specific types](delphi-checks/src/main/java/au/com/integradev/delphi/checks/InheritedTypeNameCheck.java)
   * Your own custom rules to forbid usage of
     [types](delphi-checks/src/main/java/au/com/integradev/delphi/checks/ForbiddenTypeCheck.java),
     [methods](delphi-checks/src/main/java/au/com/integradev/delphi/checks/ForbiddenMethodCheck.java),
     [properties](delphi-checks/src/main/java/au/com/integradev/delphi/checks/ForbiddenPropertyCheck.java),
     [identifiers](delphi-checks/src/main/java/au/com/integradev/delphi/checks/ForbiddenIdentifierCheck.java),
     [constants](delphi-checks/src/main/java/au/com/integradev/delphi/checks/ForbiddenConstantCheck.java),
     [enum values](delphi-checks/src/main/java/au/com/integradev/delphi/checks/ForbiddenEnumValueCheck.java), or
     [units](delphi-checks/src/main/java/au/com/integradev/delphi/checks/ForbiddenImportFilePatternCheck.java)

### Dead code analysis

Using semantic analysis, SonarDelphi can identify unused code in your project, including:
   * [Unused imports](delphi-checks/src/main/java/au/com/integradev/delphi/checks/UnusedImportCheck.java)
   * [Unused types](delphi-checks/src/main/java/au/com/integradev/delphi/checks/UnusedTypeCheck.java)
   * [Unused methods](delphi-checks/src/main/java/au/com/integradev/delphi/checks/UnusedMethodCheck.java)
   * [Unused properties](delphi-checks/src/main/java/au/com/integradev/delphi/checks/UnusedPropertyCheck.java)
   * [Unused fields](delphi-checks/src/main/java/au/com/integradev/delphi/checks/UnusedFieldCheck.java)
   * [Unused local variables](delphi-checks/src/main/java/au/com/integradev/delphi/checks/UnusedLocalVariableCheck.java)
   * [Unused constants](delphi-checks/src/main/java/au/com/integradev/delphi/checks/UnusedConstantCheck.java)

### Advanced custom rules

In addition to template rules, SonarDelphi can be extended with custom rules plugins:
  * Leverage the full power of the analysis engine with the SonarDelphi rules API.
  * For more details, refer to [Custom Rules 101](docs/CUSTOM_RULES_101.md).

## Usage

1. Install the following:
    - [SonarQube](https://docs.sonarqube.org/latest/setup/install-server/) (v9.9+)
    - [SonarScanner](https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/scanners/sonarscanner/)
    - [Delphi](https://www.embarcadero.com/products/delphi)

2. Install the plugin:
    1. Download the SonarDelphi plugin
      from [Releases](https://github.com/integrated-application-development/sonar-delphi/releases).
    2. [Install the plugin](https://docs.sonarqube.org/latest/setup/install-plugin/) on the SonarQube server.

3. Run analysis on your Delphi project:
    - [Configure](#configuration) your `sonar.delphi.installationPath`.
    - Execute `sonar-scanner` in your project's directory.

4. View analysis results:
    - Visit the link provided at the end of the scan to view analysis results on SonarQube.

> [!WARNING]
> Unfortunately, Delphi Community Edition is **not** supported.
>
> SonarDelphi requires source code for all dependencies, including the standard library.

## Configuration

You can configure SonarDelphi [analysis parameters](https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/analysis-parameters/)
on the SonarQube server at **Administration > General Settings > Delphi**.

For the full list, see [Language-specific properties](docs/CONFIGURATION.md#language-specific-properties).

> [!IMPORTANT]
> `sonar.delphi.installationPath` must point to a valid Delphi installation for the scan to succeed.

## Contributing

SonarDelphi is open for contributions, from bug reports to new features. For more details, please
read the [contributing guide](docs/CONTRIBUTING.md).

## Development

SonarDelphi can be built with JDK 17+ using [Maven](https://maven.apache.org/).

To build the project and run all tests, execute the following command from the project's root directory:

```bash
mvn clean install
```

After building, the plugin jar can be found in `sonar-delphi-plugin/target`.

## History

In 2012, [Sabre Airline Solutions](https://www.sabre.com) released SonarDelphi as an open source
project. Over the years, it has been forked and iterated on by various maintainers.

In 2018, it was picked up as a Monash University student project for [IntegraDev](https://www.integradev.com.au).
Since 2019, the project has been actively developed and extensively rewritten by IntegraDev.

## License

Licensed under the [GNU Lesser General Public License, Version 3.0](http://www.gnu.org/licenses/lgpl.txt).
