# ![SonarDelphi](docs/images/sonar-delphi-title-gh.png)

[![format](https://github.com/integrated-application-development/sonar-delphi/actions/workflows/format.yml/badge.svg?branch=master&event=push)](https://github.com/integrated-application-development/sonar-delphi/actions/workflows/format.yml)
[![build](https://github.com/integrated-application-development/sonar-delphi/actions/workflows/build.yml/badge.svg?branch=master&event=push)](https://github.com/integrated-application-development/sonar-delphi/actions/workflows/build.yml)

[SonarQube](https://www.sonarqube.org) is an open platform to manage code quality. This plugin adds Delphi support to
SonarQube.

The project was originally a [Sabre Airline Solutions](https://www.sabre.com) donation, and has been iterated on by
various open-source maintainers in the intervening years. In 2018, it was revived as a Monash University student
project for [IntegraDev](https://www.integradev.com.au).

Since 2019, the project has been maintained by IntegraDev.

Features
-------

* 100+ rules
* Metrics (complexity, number of lines, etc.)
* Import of test coverage reports (compatible with [DelphiCodeCoverage](https://sourceforge.net/p/delphicodecoverage/git/ci/master/tree/))
* Custom rules

Analyzing a Delphi project
--------------------------

1. Install [SonarQube](https://docs.sonarqube.org/latest/setup/install-server/).
   The latest release is compatible with 9.9+.
2. Download the SonarDelphi plugin from the [Releases](https://github.com/integrated-application-development/sonar-delphi/releases)
   page and [install it in SonarQube](https://docs.sonarqube.org/latest/setup/install-plugin/).
3. Install the [SonarScanner](https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/scanners/sonarscanner/).
4. Configure [project analysis settings](https://docs.sonarqube.org/latest/analysis/analysis-parameters/)
   for your Delphi project.
5. Run the SonarScanner.
6. Follow the link provided at the end of the scan to view the results.

>
> :warning: **Note**
>
> The SonarDelphi analyzer requires source code for all dependencies, including the standard library.
>
> As a result, Delphi CE is not supported.
>

Build & Test
----------------

SonarDelphi targets Java 11 and can be built with JDK 17+.

Execute the following command from the project's root directory:
```
mvn clean install
```

License
-------

Licensed under the [GNU Lesser General Public License, Version 3.0](http://www.gnu.org/licenses/lgpl.txt).
