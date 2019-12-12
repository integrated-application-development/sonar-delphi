# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Semantic analysis:
  - Symbol table
  - Type resolution
  - Overload resolution
- Search path indexation.
- Symbol information is now provided to the Sonar API for use in the web interface.
- `MemoryManagementRule` analysis rule, which flags object allocations without memory management.
- `ShortIdentifiersRule` analysis rule, which flags identifiers below a certain length.
- `AssignedNilCheckRule` analysis rule, which flags comparisons to `nil` where `Assigned` should be
  used instead.
- `CaseStatementSizeRule` analysis rule, which flags case statements with less than 2 cases.
- `DestructorDestroyRule` analysis rule, which flags destructors that aren't named `Destroy`.
- `ExplicitTObjectRule` analysis rule, which flags `class` declarations that don't explicitly
  declare their parent type.
- `MethodNestingDepthRule` analysis rule, which flags subroutines that are nested too deeply.
- `ForbiddenIdentifierRule` analysis rule template, which flags identifiers with a specified name.
- `ForbiddenMethodRule` analysis rule template, which flags methods with a specified fully-qualified
  name.
- `ForbiddenTypeRule` analysis rule template, which flags types with a specified fully-qualified
  name.
- `sonar.delphi.sources.searchPath` property to specify directories to search for include files and
  unit imports.
- `sonar.delphi.unitScopeNames` property to specify unit scope names, which are used for unit import
  resolution.
- `sonar.delphi.sources.standardLibrarySource` property to specify a path to the standard library
  source code. This is a required property.

### Changed

- Rewrite preprocessor from scratch:
  - Support for complex branching conditionals
  - Support for constant expressions in conditional directives
  - Support for `SCOPEDENUMS` switch
  - Preservation of the original token file positions
- Enhance `MixedNamesRule` with symbol table information.
- Exclude class constructors in `ConstructorCreateRule`.
- Handle `else` exception handler in `SwallowedExceptionsRule`.
- Handle global variables separately in `VariableNameRule`.
- Allow empty case branches in `EmptyBeginStatementRule` if they have comments.

### Fixed

- FPs around forward declarations in `ClassPerFileRule`.
- Various parsing bugs.
- Typo in `ClassNameRule` issue message.

## [0.8.0] - 2019-08-26

### Added

- Cognitive Complexity metric.
- `BeginEndRequiredRule` analysis rule, which flags any place where `begin..end` can be used.
- `MethodCognitiveComplexityRule` analysis rule, which flags methods that are too complex.
- `EnumNameRule` analysis rule, which flags enums that don't match an expected naming convention.
- `RedundantBooleanRule` analysis rule, which flags redundant boolean expressions.
- `XPathTemplateRule` analysis rule template, which flags AST structures matching an XPath.

### Changed

- Refactor the whole AST.
- Reimplement all analysis rules.
- Rewrite metrics calculation from scratch.

### Removed

- `IfNotFalseRule` analysis rule.
- `IfTrueRule` analysis rule.
- `NoBeginAfterDoRule` analysis rule.
- `ThenTryRule` analysis rule.

### Fixed

- Various parsing bugs.

## [0.7.0] - 2019-08-23

### Added

- Proper support for rule templates.
- `StringLiteralRegexRule` analysis rule template, which flags string literals matching a regular
  expression.

### Fixed

- Fix FPs around qualified identifiers in `MixedNamesRule`.

## [0.6.0] - 2019-08-20

### Added

- `CommentRegexRule` analysis rule template, which flags comments matching a regular expression.
- `InheritedTypeNameRule` analysis rule template, which enforces a naming convention based on the
  parent type.

### Changed

- Exclude empty `virtual` and `override` methods in `EmptyMethodRule` if they have comments.
- Include constructors and destructors in `NoSemiAfterMethodDeclarationRule`.
- Allow `TFrame` prefix in `ClassNameRule`.
- Update various rule descriptions and issue types.

## [0.5.0] - 2019-08-09

### Added

- `TooLargeMethodRule` analysis rule, which flags methods with too many statements.

### Changed

- Update rule description for `ReRaiseExceptionRule`.
- Support constructors and destructors in `TooManySubproceduresRule`.

### Removed

- `TooLongMethodRule` analysis rule.

### Fixed

- FPs and various edge-cases in `ReRaiseExceptionRule`.
- FPs around forward declarations in `EmptyInterfaceRule`.

## [0.4.0] - 2019-08-05

### Added

- `sonar.delphi.pmd.testTypeRegex` property to specify types that will be treated as test code.

### Changed

- Ignore test code in `TooLongLineRule`.
- Ignore test code in `SwallowedExceptionsRule`.

## [0.3.0] - 2019-08-01

### Changed

- Include comments in the line length in `TooLongLineRule`.
- Allow a digit to be the first character in a name (after the prefix) in the name convention rules.
- Update severity metadata for all rules.

### Fixed

- FPs around class constructors in `ConstructorWithoutInheritedStatementRule`.
- FPs around class destructors in `DestructorWithoutInheritedStatementRule`.
- FPs and various edge-cases in `DuplicatesRule`.
- Interface declarations weren't recognized properly in `InterfaceNameRule`.
- Parsing ambiguities between identifiers and keywords.

## [0.2.0] - 2019-08-01

### Added

- Copy/paste detection. (CPD)
- Syntax highlighting.

## [0.1.0] - 2019-07-29

### Added

- Support for Sonar API 7.9.1.
- Support for Java 11.
- Support for configurable conditional defines.
- `SwallowedExceptionsRule` analysis rule, which flags empty exception handlers and empty `except`
  blocks.

### Changed

- Exclude generated auto-create form variables in `VariableNameRule`.
- Allow `E` and `TForm` prefixes in `ClassNameRule`.
- Prevent empty methods from being flagged by `EmptyBeginStatementRule`.
- Better issue detection in `AssignedAndFreeRule`.
- Better issue detection in `TooManyArgumentsRule`.
- Better issue detection in `TooManyVariablesRule`.

### Removed

- `UppercaseReservedWordsRule` analysis rule.
- `WithAfterDoThenRule` analysis rule.
- `EmptyExceptBlockRule` analysis rule.

### Fixed

- File-position calculation for issues.
- Parsing issues.
- Lexing issues.
- Conditional define resolution.
- False-positives in 13 rules.
- Logic errors in existing "semantic analysis" that caused scan failures.

[Unreleased]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.8.0...HEAD
[0.8.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.7.0...v0.8.0
[0.7.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.6.0...v0.7.0
[0.6.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.5.0...v0.6.0
[0.5.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/integrated-application-development/sonar-delphi/releases/tag/v0.1.0