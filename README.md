FIT4002 IntegraDev SonarQube Delphi Project
================
This project is a SonarQube plugin for Delphi code, specifically extended and modified for use by IntegraDev. Rules have been created to address specific needs of IAD. The plugin was originally developed by Sabre Airline solutions and Fabricio Colombo. It was then extended by SandroLuck and eko1, before being forked for this project:

https://github.com/fabriciocolombo/sonar-delphi 
https://github.com/SandroLuck/SonarDelphi
https://github.com/ekot1/SonarDelphi

The current codebase was originally forked from the following repository: https://github.com/ekot1/SonarDelphi, updated to work with SonarQube 7.2+, and was moved into this repository.

The original work on the plugin that was done before forking the existing project can be found in prototype branch.

Forked code is released under GPL.

Getting Started 
==================
We highly recommend starting with the Developer Quick Start guide to get an introduction to the plugin. This readme does 
not go into the level of detail of that document. 

Implemented Rules
==================
The Following rules have been implemented by the team:

  * Avoid using with
  * Class names shoule begin with T
  * Too many classes per file
  * Method names should begin with capitals
  * Constants should begin with C_
  * Constructors should begin with create
  * Constructors should use inherited appropriately
  * Destructors should use inherited appropriately
  * If not notation
  * If true notation
  * Sorted should be called before calling Duplicates
  * Interface names should begin with I
  * Lower and upper case keywords
  * No semicolon
  * Line too long
  * Public Fields
  * Too Many function Subprocedures
  * Too many function arguments
  * Too many function variables
  * Arguments should not be unused
  * Procedures should not have too many arguments
  * Methods should not be too long
  * Methods should not have empty brackets if no arguments supplied
  * Keywords should be lowercase
  * Keywords should not be uppercase
  * Lines should not be too long
  * Record names should begin with T
  * Begin statements should follow do statements
  * Do not re-raise exceptions
  
  
Additional rules already present in the plugin have been tested to ensure they work correctly.
 
Testing Framework
==================
JUnit testing:

This testing is performed on the output of each rule against test Delphi
source files. Refer to the Quick Start guide for more details.

 * The latest plugin is compiled.
 * The compiled JAR is copied to a local installation of SonarQube. 
 * The Server is restarted with the new plugin.
 * A scan is run to create results in pmd-report.xml
 * Tests are then run again, parsing in results from pmd-report.xml
  * JUnit tests are run on these results. This tests actual output from the plugin on real pascal files.
  
Additional Documentation
==================
Most documentation was written on our Confluence server, this was backed up so a new instance could be created and additional PDF/Word versions if needed. This includes development documentation detailing how to better understand and further understand the plugin code.
Known issues are listed in the Known Issues.md file.