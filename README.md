FIT4002 IntegraDev SonarQube Delphi Project
================
This project is a SonarQube plugin for Delphi code, specifically extended and modified for use by IntegraDev. Rules have been created to address specific needs of IAD. The plugin was originally developed by Sabre Airline solutions and Fabricio Colombo. It was then extended by SandroLuck and eko1, before being forked for this project:

https://github.com/fabriciocolombo/sonar-delphi 
https://github.com/SandroLuck/SonarDelphi
https://github.com/ekot1/SonarDelphi

The current codebase was originally forked from the following repository: https://github.com/ekot1/SonarDelphi, updated to work with SonarQube 7.2+, and was moved into this repository.

The original work on the plugin that was done before forking the existing project can be found in prototype branch.

Forked code is released under GPL.

Implemented Rules
==================
The Following rules have been implemented by the team:

  * Avoid using with
  * Class names shoule begin with T
  * Constants should begin with C_
  * Constructors should call create
  * Constructors should use inherited appropriately
  * Destructors should use inherited appropriately
  * If not notation
  * Interface names
  * Lower and upper case keywords
  * No begin after Do
  * No semicolon
  * Line too long
  * Public Fields
  * Too Many Subprocedures
  * Re raise exceptions
  
  
Additional rules already present in the plugin have been tested to ensure they work correctly.
 
Testing Framework
==================
JUnit testing:

 * The latest plugin is compiled.
 * The compiled JAR is copied to a local installation of SonarQube. 
 * The Server is restarted with the new plugin.
 * A scan is run to create results in pmd-report.xml
 * Tests are then run again, parsing in results from pmd-report.xml
  * JUnit tests are run on these results. This tests actual output from the plugin on real pascal files.
  
Additional Documentation
==================
Most documentation was written on our Confluence server, this was backed up so a new instance could be created and additional PDF/Word versions if needed. This includes development documentation detailing how to better understand and further understand the plugin code.
