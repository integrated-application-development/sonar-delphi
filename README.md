# ![SonarDelphi](docs/images/sonar-delphi-title-gh.png)

[![Format](https://github.com/Integrated-Application-Development/sonar-delphi/actions/workflows/format.yml/badge.svg?branch=master&event=push)](https://github.com/Integrated-Application-Development/sonar-delphi/actions/workflows/format.yml) [![Build](https://github.com/Integrated-Application-Development/sonar-delphi/actions/workflows/build.yml/badge.svg?branch=master&event=push)](https://github.com/Integrated-Application-Development/sonar-delphi/actions/workflows/build.yml)

[SonarQube](https://www.sonarqube.org) is an open platform to manage code quality. This plugin adds Delphi support to
SonarQube.

The project was originally a [Sabre Airline Solutions](https://www.sabre.com) donation, and has been iterated on by
various open-source maintainers in the intervening years. In 2018, it was revived as a Monash University student
project for [IntegraDev](https://www.integradev.com.au).

Since 2019, the plugin has been maintained by IntegraDev.

Features
-------

* 100+ rules
* Metrics (complexity, number of lines, etc.)
* Import of test coverage reports (compatible with [DelphiCodeCoverage](https://sourceforge.net/p/delphicodecoverage/git/ci/master/tree/))
* Custom rules

Analyzing a Delphi project
--------------------------

1. Install the SonarQube server (see [SonarQube - Install the Server](https://docs.sonarqube.org/latest/setup/install-server/)).
Check the plugin releases page for supported versions of SonarQube - the latest release should be compatible with 7.9+.
2. Download the SonarDelphi plugin from the [releases page](https://github.com/Integrated-Application-Development/sonar-delphi) and install
in the SonarQube server (see [SonarQube - Install a Plugin](https://docs.sonarqube.org/latest/setup/install-plugin/)).
3. Install a [SonarScanner](https://docs.sonarqube.org/latest/analysis/overview/) of your choice.
4. Configure [project analysis settings](https://docs.sonarqube.org/latest/analysis/analysis-parameters/) for your Delphi project.
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
