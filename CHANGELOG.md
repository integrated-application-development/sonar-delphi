# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- Exclude empty `virtual` and `override` methods in `EmptyMethodRule` if they have comments.

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

[Unreleased]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.5.0...HEAD
[0.5.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/integrated-application-development/sonar-delphi/releases/tag/v0.1.0