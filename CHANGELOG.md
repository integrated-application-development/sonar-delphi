# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Support inline methods expanded via property invocations in dependency analysis.
- Support for `with` statements.

### Changed

- Support the `Result` variable in anonymous functions.
- Improve type resolution for array constructors with procedural elements.
- Improve type comparisons between ordinals and subranges with comparable base types.
- Improve type comparisons between variants and enumerations.

### Fixed

- Incorrect operator precedence for `as`.

## [0.20.0] - 2020-06-05

### Added

- Dependency analysis step that occurs during Symbol Table construction.
- `ImportSpecificityRule` analysis rule, which flags imports that could be moved to the
  implementation section.
- `AssertMessageRule` analysis rule, which flags `Assert` calls without error messages in main code.

### Changed

- Use dependency analysis in `UnusedImportsRule`.
- Improve distance-calculation between floating-point types during overload resolution.
- Penalize signed vs unsigned type mismatches during overload resolution.
- Treat `Real` as a type alias to `Double`.
- Treat `Cardinal` as a type alias to `LongWord`.
- Treat the result of binary expressions including floating-point types as `Extended`.
- Improve type comparisons between dynamic arrays.
- Increase the penalty when converting from pointer to array types.

### Fixed

- Ambiguities in the intrinsic method signatures.
- Ambiguous type comparisons from `AnsiString` to other string types.
- String types were implicitly convertible to char types.
- Objects weren't implicitly convertible to untyped pointers.
- Methods without the `overload` directive were being collected from imported units as potential
  overloads during name resolution.
- A constructor/destructor implementation could fail to find its declaration in the interface
  section if a class constructor/destructor existed in a supertype defined in the same unit with the
  same name.
- Name resolution ambiguities in cases where comparable method overloads exist in supertypes or
  other units.
- Name resolution ambiguities when an overridden (and overloaded) method had a different set of
  default parameters than its overrides.
- Various parsing bugs.

## [0.19.0] - 2020-05-21

### Changed

- Improve description of `UnusedImportsRule`.

### Fixed

- Overload resolution failures around overloads imported from multiple units.
- Non-deterministic imports when multiple units have the same name.

## [0.18.0] - 2020-05-19

### Changed

- Exclude most IDE-generated imports from `UnusedImportsRule`.

### Fixed

- Name resolution bugs around nested unscoped enums.
- Type comparison bugs around types of the form `type TFoo`.

## [0.17.0] - 2020-04-27

### Added

- `UnusedImportsRule` analysis rule, which flags imports that aren't used in the file.

### Changed

- Streamline symbol table construction into a single-pass implementation for better performance.
- Exclude records in `PublicFieldsRule`.

### Fixed

- Preprocessor bug where included tokens could be unexpectedly deleted.
- Name resolution bug where method overload searches would stop prematurely if a class
  constructor or class destructor was found with the same name.

## [0.16.2] - 2020-04-22

### Changed

- Exclude overrides and interface implementations in `MethodNameRule`.
- Treat class helper types as inheriting from `System.TClassHelperBase`.

### Fixed

- Recursion bug in import resolution.

## [0.16.1] - 2020-03-26

### Fixed

- Various name resolution bugs.

## [0.16.0] - 2020-03-17

### Added

- Support Option Set (`.optset`) references in `.dproj` files.

## [0.15.0] - 2020-03-12

### Added

- Support for `cwe` and `owasp` security standards in rule metadata.

### Changed

- Improve semantic analysis around generic types.

## [0.14.0] - 2020-03-05

### Added

- Support for generic types and methods in semantic analysis.

## [0.13.0] - 2020-02-25

### Added

- Support for `class` and `record` helpers in semantic analysis.
- Support for explicit array constructor invocations (`TArray<Integer>.Create(1, 2, 3)`).
- Support for unit aliases.
- `sonar.delphi.unitAliases` property to specify unit aliases, which are used for unit import
  resolution.

### Changed

- General improvements around analysis of types represented by keywords (`string`, `file`).
- Exclude array constructors in `EmptyBracketsRule`.
- Exclude procedural variables in `EmptyBracketsRule`.

## [0.12.1] - 2020-02-18

### Fixed

- The current type could not be resolved from within a subroutine.
- Files with multiple implementation sections (due to conditional compilation) would be preprocessed
  in unexpected ways.

## [0.12.0] - 2020-02-13

### Added

- `sonar.delphi.pmd.testSuiteType` property to specify an ancestor type for types that will be
  treated as test code.
- `sonar.delphi.conditionalUndefines` property to exclude specified defines that were aggregated
  from the project files.
- `typeIs` XPath function.
- `typeIsExactly` XPath function.
- `typeInheritsFrom` XPath function.
- Log the conditional defines used for analyzing the project.

### Removed

- `sonar.delphi.pmd.testTypeRegex` property.

### Fixed

- The name of the `Test Type Regex` property was displaying as `Generate XML Report`.
- Imports could be traversed transitively during name resolution.

## [0.11.2] - 2020-02-10

### Fixed

- Bug where analysis would sometimes skip the implementation section.

## [0.11.1] - 2020-02-05

### Changed

- Improve description of `TypeAliasRule`.

## [0.11.0] - 2020-02-05

### Added

- `ExtraneousArgumentListCommasRule` analysis rule, which flags trailing commas in argument lists.

### Changed

- Exclude interface parameters in `MemoryManagementRule`.
- Reclassify `Break`, `Continue`, and `Exit` as compiler intrinsics.
- Update default `severity` of all `BUG` rules.

### Removed

- `SpecialKeywordCapitalizationRule` analysis rule.

### Fixed

- FPs around variant conversions in `RedundantBooleanRule`.
- Bug where the base type of an enum subrange would not be resolved.

## [0.10.0] - 2020-01-17

### Changed

- Improve progress reporting.
- Improve type resolution around implicit forward declarations.
- Pointer names are now expected to take the dereferenced type name and replace a leading `T` with a
  `P` in `PointerNameRule`.
- Improve description of `EmptyBeginStatementRule`.
- Improve description of `CommentedOutCodeRule`.

### Removed

- `sonar.delphi.sources.project` property.
- `sonar.delphi.sources.workgroup` property.

### Fixed

- FPs around names ending with the word `Duplicates` in `DuplicatesRule`.
- FPs around things that look like `if` statements in `CommentedOutCodeRule`.

## [0.9.0] - 2020-01-07

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
- `SpecialKeywordCapitalizationRule` analysis rule, which flags special keywords that are not
  capitalized.
- `MultipleVariableDeclarationRule` analysis rule, which flags places where multiple variables are
  declared in a single declaration list.
- `TabulationCharactersRule` analysis rule, which flags files containing tabs.
- `EmptyTypeSectionRule` analysis rule, which flags empty `type` sections.
- `EmptyVisibilitySectionRule` analysis rule, which flags empty visibility sections.
- `EmptyFieldSectionRule` analysis rule, which flags empty field sections.
- `RedundantParenthesesRule` analysis rule, which flags redundant parentheses in expressions.
- `SuperfluousSemicolonsRule` analysis rule, which flags stray semicolons.
- `CommentedOutCodeRule` analysis rule, which flags comments containing source code.
- `ObjectTypeRule` analysis rule, which flags `object` type declarations.
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
- Exclude untyped expression casts in `CastAndFreeRule`.
- Handle `else` exception handler in `SwallowedExceptionsRule`.
- Handle global variables separately in `VariableNameRule`.
- Allow empty case branches in `EmptyBeginStatementRule` if they have comments.

### Removed

- `sonar.delphi.sources.excluded` property.

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

[Unreleased]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.20.0...HEAD
[0.20.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.19.0...v0.20.0
[0.19.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.18.0...v0.19.0
[0.18.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.17.0...v0.18.0
[0.17.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.16.2...v0.17.0
[0.16.2]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.16.1...v0.16.2
[0.16.1]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.16.0...v0.16.1
[0.16.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.15.0...v0.16.0
[0.15.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.14.0...v0.15.0
[0.14.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.13.0...v0.14.0
[0.13.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.12.1...v0.13.0
[0.12.1]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.12.0...v0.12.1
[0.12.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.11.2...v0.12.0
[0.11.2]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.11.1...v0.11.2
[0.11.1]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.11.0...v0.11.1
[0.11.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.10.0...v0.11.0
[0.10.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.9.0...v0.10.0
[0.9.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.8.0...v0.9.0
[0.8.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.7.0...v0.8.0
[0.7.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.6.0...v0.7.0
[0.6.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.5.0...v0.6.0
[0.5.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/integrated-application-development/sonar-delphi/releases/tag/v0.1.0