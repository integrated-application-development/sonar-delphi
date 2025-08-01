# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- `ObjectPassedAsInterface` analysis rule, which detects object references that are passed directly as
  an interface to a routine.
- `TooManyDefaultParameters` analysis rule, which flags routines with an excessive number of default parameters.

### Changed

- `EmptyBlock` now ignores all empty blocks containing an explanatory comment.
- `TooManyParameters` now flags routine declarations instead of implementations, improving support for methods
that are virtual, abstract, or on an interface.

## [1.17.2] - 2025-07-03

### Fixed

- Ambiguous type comparisons from variants to real types.

## [1.17.1] - 2025-06-10

### Fixed

- Inaccurate handling of variant conversions, which resulted in overload resolution failures.

## [1.17.0] - 2025-05-15

### Added

- Support for the `AtomicCmpExchange128` intrinsic routine.
- Support for the `GetTypeKind` intrinsic routine.
- Support for the `OpenString` intrinsic type.
- **API:** `TypeParameterNode::getTypeParameters` method.
- **API:** `InterfaceTypeNode::getGuidExpression` method.
- **API:** `AttributeNode::getExpression` method.
- **API:** `IntrinsicType.OPENSTRING` enum value.

### Changed

- Issue locations no longer span the entire routine declaration in `RoutineName`.
- Improve type modeling around the `VarArg*` intrinsic routines.

### Deprecated

- **API:** `InterfaceGuidNode` node type.
- **API:** `InterfaceTypeNode::getGuid` method, use `getGuidExpression` instead.
- **API:** `DelphiTokenType.GUID`, as the associated `InterfaceGuidNode` is no longer parsed.

### Fixed

- Name resolution failures on generic routine invocations where later type parameters are constrained by earlier type parameters.
- Type resolution failures on property attribute lists.
- Type resolution failures on implementation-local routine attribute lists.
- Type resolution failures on `as` casts where the type is returned from a routine invocation.
- Inaccurate type resolution when calling a constructor on a class reference type.
- Grammar ambiguity causing attributes to be misinterpreted as interface GUIDs.
- Failure to resolve invocations of `System.IsManagedType` where a value is passed.

## [1.16.0] - 2025-05-09

### Added

- Support for MSBuild item and item metadata expressions in project files.
- Debug logging for name resolution failures (enabled with `sonar-scanner -X`).
- `ExhaustiveEnumCase` analysis rule, which flags `case` statements that do not handle all values in an enumeration.
- `IterationPastHighBound` analysis rule, which flags `for` loops that iterate past the end of the collection.
- `ExplicitBitwiseNot` analysis rule, which flags potentially incorrect bitwise `not` operations.
- **API:** `EnumeratorOccurrence` type.
- **API:** `ForInStatementNode::getEnumeratorOccurrence` method.
- **API:** `TypeOfTypeNode::getTypeReferenceNode` method.
- **API:** `NameReferenceNode::getFirstName` method.
- **API:** `DelphiTokenType.TYPE_CONSTRAINT` token type.
- **API:** `ConstraintNode` node type.
- **API:** `ClassConstraintNode` node type.
- **API:** `ConstructorConstraintNode` node type.
- **API:** `RecordConstraintNode` node type.
- **API:** `TypeConstraintNode` node type.
- **API:** `TypeParameterNode::getConstraintNodes` method.
- **API:** `TypeParameterType::constraintItems` method.
- **API:** `Constraint` type.
- **API:** `Constraint.ClassConstraint` type.
- **API:** `Constraint.ConstructorConstraint` type.
- **API:** `Constraint.RecordConstraint` type.
- **API:** `Constraint.TypeConstraint` type.

### Changed

- Detect tab-indented multiline strings in `TabulationCharacter`.
- Improve support for evaluating name references in compiler directive expressions.
- Improve overload resolution in cases involving generic type parameter constraints.
- Improve handling for MSBuild properties, items, and conditional evaluation.
- Perform best-effort name resolution of arguments to unresolved routines or array properties, which
  improves analysis quality in cases where symbol information is incomplete.

### Deprecated

- **API:** `ForInStatementNode::getGetEnumeratorDeclaration` method, get the declaration through
  `getEnumeratorOccurrence` instead.
- **API:** `ForInStatementNode::getMoveNextDeclaration` method, get the declaration through
  `getEnumeratorOccurrence` instead.
- **API:** `ForInStatementNode::getCurrentDeclaration` method, get the declaration through
  `getEnumeratorOccurrence` instead.
- **API:** `TypeParameterNode::getTypeConstraintNodes` method, use `getConstraintNodes` instead.
- **API:** `TypeParameterType::constraints` method, use `constraintItems` instead.

### Fixed

- False positives on enumerable method `GetEnumerator` in `UnusedRoutine`.
- False positives on enumerator method `MoveNext` in `UnusedRoutine`.
- False positives on enumerator property `Current` in `UnusedProperty`.
- False positives on enums that are never referenced by name (but have used values) in `UnusedType`.
- Name resolution failures in legacy initialization sections referencing the implementation section.
- Name resolution failures when accessing ancestors of enclosing types from nested type methods.
- Name resolution failures on invocations of methods with generic open array parameters.
- Name resolution failures around `Create` calls on types with `constructor` constraints.
- Name resolution failures on `read`, `write`, and `stored` specifiers of indexed properties.
- Name resolution failures on routine references assigned to a `var`/`const` declaration.
- Name resolution failures on routine references in constant arrays.
- Type resolution failures on array expressions nested within multidimensional constant arrays.
- Incorrect file position calculation for multiline string tokens.
- Analysis errors around `type of` type declarations.

## [1.15.0] - 2025-04-03

### Added

- `ParsingError` analysis rule, which flags files where parsing failures occurred.
- Support for the `WEAK_NATIVEINT` symbol, which is defined from Delphi 12 onward.
- Support for undocumented intrinsics usable within `varargs` routines:
  - `VarArgStart`
  - `VarArgGetValue`
  - `VarArgCopy`
  - `VarArgEnd`

### Changed

- Exclude types annotated with attributes in `UnusedType`.
- Improve reporting of parsing failures occurring within include files.
- Improve reporting of parsing failures occurring within compiler directives.

### Deprecated

- **API:** `SwitchDirective.SwitchKind.TYPEADDRESS` enum member, use `TYPEDADDRESS` instead.

### Fixed

- Exceptions from empty structures (e.g., `if`) in `LoopExecutingAtMostOnce` and `RedundantJump`.
- False positives from case statements in `LoopExecutingAtMostOnce`.
- False positives from nested finally-except blocks in `RedundantJump`.
- False positives around wrapped type declarations in `VisibilityKeywordIndentation`.
- False negatives around inline `var` and `const` in `PlatformDependentTruncation`.
- Trailing whitespace within comments not recognized in `TrailingWhitespace`.
- Overload resolution failures on the variant overload of the `Copy` intrinsic.
- Several compiler directives were not being recognized:
  - `E`
  - `F`
  - `K`
  - `N`
  - `S`
  - `ALLOWBIND`
  - `ALLOWISOLATION`
  - `HIGHENTROPYVA`
  - `HIGHCHARUNICODE`
  - `LARGEADDRESSAWARE`
  - `MAXPAGESIZE`
  - `NXCOMPAT`
  - `SONAME`
  - `SOPREFIX`
  - `SOSUFFIX`
  - `SOVERSION`
  - `STACKCHECKS`
  - `STRINGCHECKS`
  - `TSAWARE`
  - `TYPEDADDRESS`

## [1.14.1] - 2025-03-05

### Fixed

- File pointer errors around `>=` and `<=` tokens.

## [1.14.0] - 2025-03-03

### Added

- `RedundantJump` analysis rule, which flags redundant jump statements, e.g., `Continue`, `Exit`.
- `LoopExecutingAtMostOnce` analysis rule, which flags loop statements that can execute at most once.
- `excludedNames` rule property to the `MixedNames` rule.
- **API:** `RepeatStatementNode::getGuardExpression` method.
- **API:** `RepeatStatementNode::getStatementList` method.
- **API:** `CaseStatementNode::getSelectorExpression` method.
- **API:** `CaseItemStatementNode::getStatement` method.

### Fixed

- Parsing errors where adjacent `>` and `=` tokens were wrongly interpreted as the `>=` operator.
- False-positives within assignment statements in `IndexLastListElement`.

## [1.13.0] - 2025-02-05

### Added

- **API:** `AnonymousMethodNode::getDeclarationSection` method.
- **API:** `AnonymousMethodNode::getStatementBlock` method.
- **API:** `AnonymousMethodNode::isEmpty` method.

### Changed

- Include the global browsing path in unit import resolution.
- Reprioritize the analysis search path in the following order (highest to lowest):
  - Referenced project files (`DCCReference`)
  - Search path (`DCC_UnitSearchPath`)
  - Debugger source path (`Debugger_DebugSourcePath`)
  - Library path (`DelphiLibraryPath`/`DelphiTranslatedLibraryPath`)
  - Browsing path (`DelphiBrowsingPath`)
  - Standard library
  - Analysis source files (`sonar.sources`)
- Empty anonymous methods are now ignored in `EmptyBlock`.
- Empty anonymous methods are now flagged in `EmptyRoutine`.

## [1.12.2] - 2025-01-06

### Fixed

- Incorrect case-sensitive treatment of file names in coverage reports.

## [1.12.1] - 2024-12-06

### Fixed

- Various intrinsic routines had incorrect signatures around dynamic and open arrays.
- False positives around platform-dependent binary expressions in `PlatformDependentTruncation`.
- Incorrect type resolution around array property accesses.

## [1.12.0] - 2024-12-02

### Added

- **API:** `DelphiCheckContext::getUri` method.

### Fixed

- Parsing errors on semicolon-delimited generic type parameters in routine implementation headers.
- Incorrect return types for `Length`, `High`, and `Low` on open/dynamic arrays depending on the
  compiler version and toolchain.
- Name resolution failures on explicit references to default array properties with overloads on
  ancestor types.

## [1.11.0] - 2024-11-04

### Added

- `MissingRaise` analysis rule, which flags exception constructions for which `raise` has been omitted.
- **API:** `ProceduralType::directives` method.
- **API:** `ProceduralTypeNode::getDirectives` method.
- **API:** `ProceduralTypeNode::hasDirective` method.
- **API:** `ProceduralTypeHeadingNode::getDirectives` method.
- **API:** `AnonymousMethodNode::getAnonymousMethodHeading` method.
- **API:** `AnonymousMethodNode::getDirectives` method.
- **API:** `AnonymousMethodNode::hasDirective` method.
- **API:** `AnonymousMethodHeadingNode` node type.
- **API:** `DelphiTokenType.ANONYMOUS_METHOD` token type.
- **API:** `DelphiTokenType.ANONYMOUS_METHOD_HEADING` token type.

### Fixed

- Parsing errors on `implementation` keywords nested in conditional branches.
- Parsing errors on anonymous methods with routine directives (like calling conventions).
- `IndexOutOfBoundsException` errors around procedural types declared with `varargs` in
  `VariableInitialization`.

## [1.10.0] - 2024-10-01

### Added

- `FullyQualifiedImport` analysis rule, which flags non-fully qualified imports.
- **API:** `UnitImportNameDeclaration::isAlias` method.

### Changed

- Non-trivial `inherited` expressions are excluded in `RedundantParentheses`.

### Fixed

- False positives around const array literals in `RedundantParentheses`.

## [1.9.0] - 2024-09-03

### Added

- **API:** `CaseItemStatementNode::getExpressions` method.

### Changed

- Improve type inference around unsigned integer literals.
- Improve type inference around array constructors containing integer literals.
- Improve type inference around real expressions.
- Parentheses enclosing a primary expression are considered redundant in `RedundantParentheses`.
- `out` parameters are treated as uninitialized at the start of a routine in
  `VariableInitialization`.

### Fixed

- Name resolution errors around alias ancestor types.
- Parsing errors around partial `asm` blocks blocks in conditional branches.
- False positives around case items with multiple values in `CaseStatementSize`.

## [1.8.0] - 2024-08-02

### Added

- "Correct to (correct case)" quick fix for `LowercaseKeyword`.
- **API:** `PropertyNameDeclaration::getImplementedTypes` method.
- **API:** `PropertyNode::getDefaultSpecifier` method.
- **API:** `PropertyNode::getImplementsSpecifier` method.
- **API:** `PropertyNode::getIndexSpecifier` method.
- **API:** `PropertyNode::getStoredSpecifier` method.
- **API:** `PropertyDefaultSpecifierNode` node type.
- **API:** `PropertyImplementsSpecifierNode` node type.
- **API:** `PropertyIndexSpecifierNode` node type.
- **API:** `PropertyStoredSpecifierNode` node type.

### Changed

- Include the global library path in unit import resolution.
- Improve AST modeling around property specifiers.
- Improve semantic analysis around property specifiers.
- Exclude properties with `implements` specifiers in `UnusedProperty`.

### Fixed

- Overly permissive parsing rules around `string` and `file` types.
- Quick fixes removing comments and compiler directives in `RedundantInherited`.
- False positives around `message` handler methods in `RedundantInherited`.
- False positives when parameter names were mismatched to the inherited method in `RedundantInherited` and
  `InheritedMethodWithNoCode`.
- **API:** `ArrayConstructorNode::getImage` returned an incorrect image containing `[` as an element.

## [1.7.0] - 2024-07-02

### Added

- `RedundantInherited` analysis rule, which flags redundant `inherited` statements that can be safely removed.

### Changed

- Include "found" and "expected" values for issue messages in `LowercaseKeyword`.
- Exclude `J` and `K` by default in `ShortIdentifier`.
- Improve compiler directive parsing in cases where the directive name is not followed by whitespace.

### Fixed

- The `result` attribute in NUnit test reports was not being treated as case-insensitive.
- Scan failures on nested compiler directives.

## [1.6.0] - 2024-05-31

### Added

- "Move to implementation section" quick fix for `ImportSpecificity`.
- "Remove unused import" quick fix for `UnusedImport`.
- **API:** `UsesClauseNode::getImports` method.
- **API:** `InterfaceSectionNode::getUsesClause` method.
- **API:** `ImplementationSectionNode::getUsesClause` method.
- **API:** `LabelNameDeclaration` symbol declaration type.
- **API:** `DelphiScope::getLabelDeclarations` method.
- **API:** `LabelDeclarationNode` node type.
- **API:** `LabelStatementNode::getNameReference` method.
- **API:** `LabelStatementNode::getStatement` method.
- **API:** `GotoStatementNode::getNameReference` method.

### Changed

- Improve semantic analysis around `label` and `goto` statements.
- Improve handling for dereferenced types starting with `E` and `I` in `PointerName`.
- Improve handling for dereferenced types that don't comply with naming conventions in `PointerName`.

### Fixed

- File pointer errors when an issue is raised on the first line of a file with a byte order mark.
- Symbol table construction errors for program files omitting the `program` header.
- Parsing errors on parenthesised anonymous methods.
- Parsing errors on identifiers named `Label`, which was allowed in Delphi.NET.
- Scan failures on invalid paths in dproj files.
- Incorrect ordering of edits in the "Separate grouped parameters" quick fix for `GroupedParameterDeclaration`.
- SonarLint registration of rules that don't support execution in a SonarLint context.

## [1.5.0] - 2024-05-02

### Added

- Support for the `winapi` calling convention.
- Support for quick fixes.
- "Remove the superflous semicolon" quick fix for `SuperfluousSemicolon`.
- "Correct to (correct case)" quick fix for `MixedNames`.
- "Remove redundant parentheses" quick fix for `RedundantParentheses`.
- "Convert to initialization section" quick fix for `LegacyInitializationSection`.
- "Replace Pascal-style result with Result" quick fix for `PascalStyleResult`.
- "Replace with TList.Last" quick fix for `IndexLastListElement`.
- "Remove empty argument list" quick fix for `EmptyArgumentList`.
- "Remove redundant assignment check" quick fix for `AssignedAndFree`.
- "Separate grouped parameters" quick fix for `GroupedParameterDeclaration`.
- "Remove (n) unused formatting arguments" quick fix for `FormatArgumentCount`.
- "Use string value directly" quick fix for `FormatArgumentCount`.
- **API:** `TryStatementNode::getExceptBlock` method.
- **API:** `WhileStatementNode::getGuardExpression` method.
- **API:** `WhileStatementNode::getStatement` method.
- **API:** `DelphiTokenType.WINAPI` token type.
- **API:** `DelphiIssueBuilder` type, which is returned by `DelphiCheckContext::newIssue`.
- **API:** `QuickFix` type, which is accepted by `DelphiIssueBuilder::withQuickFixes`.
- **API:** `QuickFixEdit` type, which is accepted by `QuickFix::addEdits`.

### Deprecated

- **API:** `TryStatementNode::getExpectBlock` method, use `getExceptBlock` instead.

### Fixed

- False positive `FormatStringValid` issues on strings containing uppercase format specifiers.
- Exception when parsing fully qualified attribute references.
- `DuplicatedDeclarationException` errors caused by some local scopes being modeled incorrectly.
- Name resolution issues around `if`, `else`, `for`, and `with` constructs when the body contains a single statement.

## [1.4.0] - 2024-04-02

### Added

- Support for multiline string literals within compiler directives.
- Support for the `TEXTBLOCK` directive.
- Support for named arguments.
- Support for `assembly` attributes.
- **API:** `CompilerDirectiveParser` can now return a new `TextBlockDirective` type.
- **API:** `CheckVerifier::withCompilerVersion` method.
- **API:** `CheckVerifier::withToolchain` method.
- **API:** `DelphiTokenType.ARGUMENT` token type.
- **API:** `DelphiTokenType.ASSEMBLY` token type.
- **API:** `ArgumentNode` node type.
- **API:** `ArgumentListNode::getArgumentNodes` method.
- **API:** `AttributeNode::isAssembly` method.

### Changed

- `NativeInt` and `NativeUInt` are now treated as weak aliases in Delphi 12+.
- The length of open arrays is now modeled as `NativeInt` in Delphi 12+.
- Performance improvements.

### Deprecated

- **API:** `ArgumentListNode::getArguments` method, use `getArgumentNodes` instead.

### Fixed

- Type resolution inaccuracies around subtract expressions.
- Name resolution did not occur for the magic `Width` and `Decimals` arguments.
  (See: [System.Write](https://docwiki.embarcadero.com/Libraries/en/System.Write))
- The wrong inherited method could be found in `InheritedMethodWithNoCode`, causing false negatives.
- Exception when scanning UNC paths.

## [1.3.0] - 2024-03-01

### Added

- Support for multiline string literals.
- Support for numeric literals prefixed by ampersands.
- Support for identifiers prefixed by more than 2 ampersands.
- **API:** `TextLiteralNode::isMultiline` method.
- **API:** `TextLiteralNode::getValue` method, which returns the effective contents of a text
  literal.

### Changed

- `TextLiteralNode::getImage` now returns the text literal exactly as it appears in source code.
- `TextLiteralNode::getImageWithoutQuotes` now simply calls the new `getValue` method.

### Deprecated

- `TextLiteralNode::getImageWithoutQuotes`, use `getValue` instead.
- `DelphiTokenType.AMPERSAND`, as `&` is now lexed directly into numeric literals and identifiers.

### Fixed

- Static char arrays weren't accepted for `'%s'` in `FormatArgumentType`.
- Imaginary tokens could be erroneously matched by the lexer in valid source code.

## [1.2.0] - 2024-02-02

### Added

- Support for on-demand plugin downloading, which is being
  [introduced in SonarQube 10.4](https://community.sonarsource.com/t/the-sonarscanners-download-only-required-3rd-party-plugins/108156).
- Support for the `LLVM` symbol, which is defined on LLVM-based toolchains from Delphi 12 onward.
- Support for the `IOSSIMULATOR` symbol, which is defined on the `DCCIOSSIMARM64` toolchain.
- `FormDfm` analysis rule, which flags VCL forms/frames that lack a `.dfm` resource.
- `FormFmx` analysis rule, which flags FireMonkey forms/frames that lack a `.fmx` resource.
- `FormatArgumentCount` analysis rule, which flags `Format` calls where the number of format
  arguments does not match the number of format specifiers.
- `FormatArgumentType` analysis rule, which flags `Format` arguments with types that are
  incompatible with their corresponding format specifiers.
- `FormatStringValid` analysis rule, which flags `Format` calls with invalid format strings.
- **API:** `CompilerDirectiveParser` now returns a new `ResourceDirective` type when parsing
  [resource directives](https://docwiki.embarcadero.com/RADStudio/en/Resource_file_(Delphi)).

### Changed

- Alternative casings `Writeln` and `Readln` are now allowed in `MixedNames`.
- Improve clarity of the rule description for `EmptyRoutine`.

### Fixed

- Parsing errors on `.dpr` files without a top-level `begin`.
- Symbol table errors on declarations that shared a name with a unit import.
- Symbol table executor failures on include directives that include multiple symbol declarations or
  references.
- The `Copy` intrinsic inferred an incorrect return type for `PChar`, `PAnsiChar`, and variants.
- The `Concat` intrinsic inferred an incorrect return type for single-character string literals.
- The `ReadLn` intrinsic was missing the standard input overload.
- Ideographic space (U+3000) was erroneously accepted as a valid identifier character.
- Duplicate imports in a `requires` clause now log a warning instead of throwing an exception.
- NPE on anonymous records in `PublicField`.

## [1.1.0] - 2024-01-02

### Added

- Support for unicode characters in identifiers.
- Support for the NUnit 2.0 test results format (in addition to existing 3.0 support).
- `ConsecutiveVarSection` analysis rule, which flags consecutive `var` sections that can be merged.
- `ConsecutiveConstSection` analysis rule, which flags consecutive `const` sections that can be
  merged.
- `ConsecutiveTypeSection` analysis rule, which flags consecutive `type` sections that can be
  merged.
- `ConsecutiveVisibilitySection` analysis rule, which flags consecutive visibility sections that can
  be merged.
- `excludeApi` rule property to most of the `Unused*` rules:
  - Available for `UnusedConstant`, `UnusedField`, `UnusedGlobalVariable`, `UnusedProperty`,
    `UnusedRoutine`, and `UnusedType`.
  - Excludes public API (declared with public visibility in the interface section).
- **API:** `VarSectionNode::isThreadVarSection` method.
- **API:** `ConstSectionNode::isResourceStringSection` method.
- **API:** `AttributeListNode::getAttributeTypes` method.
- **API:** `RoutineNameDeclaration::getAttributeTypes` method.
- **API:** `PropertyNameDeclaration::getAttributeTypes` method.

### Changed

- Improve type resolution on binary and unary expressions.
- Improve type comparisons between signed and unsigned integer types.
- Improve type comparisons between text types.
- Improve type conversions from character pointers to strings.
- Exclude routines annotated with attributes in `UnusedRoutine`.
- Exclude properties annotated with attributes in `UnusedProperty`.
- Exclude fields annotated with attributes in `UnusedField`.
- Improve type modeling around integer subranges.
- Issues raised on a hard cast expression now span the entire expression in `UnicodeToAnsiCast`,
  `CharacterToCharacterPointerCast`, `NonLinearCast`, `RedundantCast`, and `PlatformDependentCast`.
- Parsing errors now consistently surface the file and line where they occurred.
- Parsing errors now consistently skip the offending unit instead of crashing the scan.
- **API:** `TypeSectionNode` now implements `Visibility`.
- **API:** `TypeDeclarationNode` now implements `Visibility`.

### Fixed

- The `sonar.sourceEncoding` option was used blindly in all cases, even when the correct encoding
  could be detected.
- Test sources were erroneously excluded from analysis.
- Key information was missing from "ambiguous declarations" warnings.
- Stack overflow on class reference types that reference their containing type.
- Scan failures on redundant unit aliases in .dproj files.
- Incorrect file position calculation for multiline compiler directives.
- Incorrect detection of method calls as hard casts in `CastAndFree`.
- Name resolution failures around helpers extending weak alias types.
- Various intrinsic routine signatures had incorrect return types.
- Various intrinsic routine signatures had incorrect or overly-restrictive parameter types.

## [1.0.0] - 2023-11-14

### Added

- Custom rules plugin API:
  - See: [Writing Custom Delphi Rules](docs/CUSTOM_RULES.md)
- Rule metadata for the new [Clean Code Taxonomy](https://docs.sonarsource.com/sonarqube/10.2/user-guide/clean-code/).
- `sonar.delphi.file.suffixes` property to specify the file extensions that should be analyzed.
- `sonar.delphi.exclusions` property to specify file path patterns to exclude from analysis of
  Delphi files.
- `sonar.delphi.test.attribute` property to specify an attribute that will cause annotated types to
  be treated as test code.
- Support for the `DCCOSXARM64` toolchain.
- Support for the `DCCIOSSIMARM64` toolchain.
- Support for attributes in semantic analysis.
- Support for identifiers prefixed with 2 ampersands (`&&`).
- `OleVariant` overloads for `VarArrayRedim` and `VarClear` intrinsics.
- `InlineAssembly` analysis rule, which flags inline assembly usage.
- `DigitSeparator` analysis rule, which flags numeric literals that should use digit separators to
  improve readability.
- `DigitGrouping` analysis rule, which flags numeric literals that use non-standard digit groupings.
- `AddressOfCharacterData` analysis rule, which flags attempts to manually get the address of the
  first character in a string.
- `NonLinearCast` analysis rule, which flags unsafe object and pointer casts.
- `IndexLastListElement` analysis rule, which flags places where `TList.Last` should be used instead
  of manually indexing into the list.

### Changed

- Rename all rule keys. (For example, `ClassNameRule` is now `ClassName`)
- Rewrite all rule descriptions.
- Rename plugin key from `delphi` to `communitydelphi`.
- Rename language key from `delph` to `delphi`.
- Rename repository key from `delph` to `community-delphi`.
- Rename repository from `Delphi` to `Community Delphi`.
- Rename `sonar.delphi.bds.path` property to `sonar.delphi.installationPath`.
- Rename `sonar.delphi.compiler.toolchain` property to `sonar.delphi.toolchain`.
- Rename `sonar.delphi.compiler.version` property to `sonar.delphi.compilerVersion`.'
- Rename `sonar.delphi.sources.searchPath` property to `sonar.delphi.searchPath`.'
- Rename `sonar.delphi.testSuiteType` property to `sonar.delphi.testType`.
- Clean up descriptions of all plugin properties.
- Exclude dpr and dpk files in `UnusedImport`.
- Exclude dpr and dpk files in `ImportSpecificity`.
- Exclude uses clauses of dpr and dpk files in `LineTooLong`.
- Include routines in `UnitLevelKeywordIndentation`.
- Include implicit attribute constructor calls in `ForbiddenRoutine`.
- Always enforce the `Attribute` suffix in `AttributeName`.
- Allow the `Attribute` suffix to be omitted for attribute references in `MixedNames`.
- Allow the extended type name to be anywhere in the helper name (rather than requiring it at the
  start) in `HelperName`.
- Take a fully-qualified type name for the parent type in `InheritedTypeName`, instead of a regular
  expression.
- Improve name resolution for declarations within types.
- Improve type resolution for array accesses into variants.
- Improve type resolution around constructor calls following array accesses in primary expressions.
- Improve parsing and type modeling around `AnsiString` types with specified code pages.
- Improve type modeling around type aliases.

### Removed

- `XPathTemplateRule` analysis rule template.
- `MemoryManagementRule` analysis rule.
- `EmptyTypeSection` analysis rule.
- `sonar.delphi.coverage.tool` property.
  ([Delphi Code Coverage](https://github.com/DelphiCodeCoverage/DelphiCodeCoverage) is the only supported coverage tool.)

### Fixed

- `OSX(64)` symbols weren't treated as defined on macOS.
- `MACOS(32|64)` symbols weren't treated as defined on iOS.
- Name collisions between generic and non-generic forward declarations.
- Incorrect type conversions from pointers to arrays.
- Fixed arrays were overly-eager to convert to dynamic arrays.
- Parsing errors around `In` operator overloads.
- Parsing errors around anonymous methods in array constructor expressions.
- `Self` was not consistently shadowed by method parameters.
- Incorrect token range calculation for binary expression nodes.
- Units referenced via `DCCReference` tags were not indexed in the symbol table.
- NPE on empty argument lists in `TrailingCommaArgumentList`.
- `add` is no longer colorized as a keyword in the SonarQube web interface.
- `remove` is no longer colorized as a keyword in the SonarQube web interface.
- `variant` is no longer colorized as a keyword in the SonarQube web interface.
- Parsing errors on unusual whitespace characters.
- Parsing errors on unusual escaped character, hex integer, and binary integer literals.

## [0.40.0] - 2023-01-10

### Added

- Support for `NCLOC_DATA` metric to record which lines contain code.
- `HelperNameRule` analysis rule, which flags helper types that don't match an expected naming
  convention.
- `MemberDeclarationOrderRule` analysis rule, which flags visibility sections that violate the style
  guide's instructions on [Class Body Organization](https://docwiki.embarcadero.com/RADStudio/Alexandria/en/Type_Declarations#Class_Body_Organization).
- `VisibilitySectionOrderRule` analysis rule, which flags visibility sections that aren't in
  ascending order of accessibility.
- `UnitLevelKeywordIndentationRule` analysis rule, which flags unit-level keywords that aren't flush
  with the margin.
- `VisibilityKeywordIndentationRule` analysis rule, which flags visibility specifier directives that
  don't use the same indentation as the type they're declared under.

### Changed

- Compare unit references to original name in `MixedNamesRule`.
- Only raise issues on variables in `AssignedNilCheckRule`.
- Handle cases where the visibility of a method changes in `InheritedMethodWithNoCodeRule`.
- Handle `reintroduce`/`virtual` in `InheritedMethodWithNoCodeRule`.
- Update description for `EnumNameRule`.
- Improve type comparisons between class references and pointers.
- Improve type comparisons between records and pointers.

### Fixed

- Clean up some internal assumptions about windows paths.
- Import name-collision issues, by ensuring search paths are traversed in order of declaration.
- Type identity issues around implicit forwarded types.
- Parsing errors on `record helper for file`.
- Type resolution issues around `set of AnsiChar` literals.
- NPE in `VariableInitializationRule`.
- Exception when no dproj or groupproj files are supplied.

## [0.39.1] - 2022-11-22

### Changed

- Exclude record class variables in `VariableInitializationRule`.

## [0.39.0] - 2022-11-11

### Added

- `AttributeNameRule` analysis rule, which flags custom attribute types that don't match an expected
  naming convention.

### Changed

- Include unit imports in `MixedNamesRule`.
- Exclude parameters on methods implementing interfaces in `VariableNameRule`.
- Exclude custom attribute types in `ClassNameRule`.

### Fixed

- Allow const expressions in record alignment syntax.

## [0.38.0] - 2022-11-07

### Added

- Support for `Round` operator overloads.
- Support for `Trunc` operator overloads.
- `GroupedFieldDeclarationRule` analysis rule, which flags places where fields aren't declared
  individually.
- `GroupedParameterDeclarationRule` analysis rule, which flags places where parameters aren't
  declared individually.
- `GroupedVariableDeclarationRule` analysis rule, which flags places where variables aren't declared
  individually.

### Changed

- Detect user-created `IfThen` functions more effectively in `IfThenShortCircuitRule`.
- Update various rule descriptions with references to the Embarcadero style guide.
- Include `Debugger_DebugSourcePath` in the analysis search path.

### Removed

- `MultipleVariableDeclarationRule` analysis rule.

### Changed

- Include `string` in `LowerCaseReservedWordsRule`.
- Add `excludedKeywords` property to `LowerCaseReservedWordsRule`.

### Fixed

- The `string` keyword wasn't colorized in the SonarQube web interface.

## [0.37.1] - 2022-10-11

### Changed

- Improve overload resolution around implicit conversions.

## [0.37.0] - 2022-09-07

### Added

- Support for Delphi 11 language features
  - Binary literals
  - Digit separators

## [0.36.0] - 2022-08-19

### Added

- `CharacterToCharacterPointerCastRule` analysis rule, which flags `Char` -> `PChar` casts.
- `UnicodeToAnsiCastRule` analysis rule, which flags explicit casts to Ansi types.

## [0.35.0] - 2022-08-05

### Added

- Support for merging coverage reports.

### Changed

- Rewrite project file parsing from scratch:
  - Support for property interpolation
  - Support for MSBuild conditionals
- Exclude the `Tools` folder from standard library indexation.
- Exclude case statements in `BeginEndRequiredRule`.
- Exclude `.dpk` files in `EmptyUnitRule`.

## [0.34.2] - 2022-03-03

### Added

- `AnsiString` overload for the `SetLength` intrinsic.

### Changed

- Improve name resolution around pointers to procedural variables.
- Improve overload resolution around overloaded method references.

### Fixed

- Assignment to a field or property of a variable would not be recognized as usage of that variable
  in `UnusedLocalVariablesRule`.

## [0.34.1] - 2022-02-22

### Changed

- Include assigned variables in `UnusedLocalVariablesRule`.
- Count usage of redeclared properties in `UnusedPropertiesRule`.
- Improve type specialization for generics with generic type parameters.
- Improve overload resolution around pointers to methods.

## [0.34.0] - 2022-02-03

### Added

- `UnusedLocalVariablesRule` analysis rule, which flags unused local variables.
- `UnusedFieldsRule` analysis rule, which flags unused fields.
- `UnusedConstantsRule` analysis rule, which flags unused constants.
- `UnusedGlobalVariablesRule` analysis rule, which flags unused global variables.
- `UnusedTypesRule` analysis rule, which flags unused types.
- `UnusedPropertiesRule` analysis rule, which flags unused properties.
- `UnusedMethodsRule` analysis rule, which flags unused methods.

### Changed

- Improve name resolution around array index types.

### Removed

- `UnusedArgumentsRule` analysis rule.

### Fixed

- Type resolution bugs around "address of procedural value" expressions.
- Usages of default array properties would not be properly reflected in the symbol table.

## [0.33.0] - 2022-01-10

### Added

- Support for NUnit report format.

### Removed

- Support for surefire report format.
- `sonar.delphi.codecoverage.excluded` property.

## [0.32.0] - 2021-09-03

### Added

- `MathFunctionSingleOverloadRule` analysis rule, which flags calls to the `Single` overloads of the
  standard math functions.
- `AddressOfNestedMethodRule` analysis rule, which flags instances where subroutines are treated as
  procedural values via the address-of `@` operator.
- `FreeAndNilTObjectRule` analysis rule, which flags `FreeAndNil` arguments that aren't `TObject`
  instances.
- `VariableInitializationRule` analysis rule, which flags places where variables are used without
  being initialized first.
- `TextFile` intrinsic type, which is an alias to `Text`.

### Changed

- Improve type comparisons from integer to floating point types.
- Improve type comparisons from `Currency` to integer types.
- Improve type comparisons betweeen floating point types.
- Improve name and overload resolution around "address of procedural value" expressions.
- Improve overload resolution around numeric type mismatches. (integer vs floating point)
- Improve modeling of intrinsic methods to include parameter specifiers. (`out`, `var`, `const`)
- Improve handling of `type` types in `PlatformDependentCastRule`.
- Improve handling of `type` types in `PlatformDependentTruncationRule`.
- Clean up property names and descriptions.

### Fixed

- Overload resolution could get confused by similar generic specializations.
- Treat 8-byte `Extended` as a unique type:
  - `Extended` is 8 bytes on `DCC64` and several other toolchains.
  - Contrary to the Embarcadero
    [documentation](https://docwiki.embarcadero.com/RADStudio/Rio/en/Delphi_Considerations_for_Multi-Device_Applications#The_Extended_Data_Type_is_different_on_different_platforms),
    8-byte `Extended` is not an alias to Double.

## [0.31.1] - 2021-06-30

### Changed

- Handle casts involving strings and arrays in `PlatformDependentCastRule`.
- Update rule description for `EmptyMethodRule`.

### Fixed

- Parsing errors on label statements without any corresponding "marked statement".

## [0.31.0] - 2021-05-12

### Added

- `PlatformDependentTruncationRule` analysis rule, which flags places where an integer type may or
  may not be truncated depending on the platform.
- `EmptyUnitRule` analysis rule, which flags units that don't contain any meaningful code.
- `LegacyInitializationSectionRule` analysis rule, which flags legacy initialization sections
  (`begin`..`end` instead of `initialization`).
- `FormatSettingsInitializationRule` analysis rule, which flags `TFormatSettings` variables that
  aren't initialized before use.
- `DateFormatSettingsRule` analysis rule, which flags places where the default `TFormatSettings` is
  implicitly used.
- `ForbiddenFieldRule` analysis rule template, which flags fields from a specified list of names on
  a type with a specified fully-qualified name.
- `ForbiddenConstantRule` analysis rule template, which flags constants from a specified list of
  names from a unit with a specified fully-qualified name.
- `ForbiddenEnumValueRule` analysis rule template, which flags enum values from a specified list of
  names on an enum type with a specified fully-qualified name.

### Changed

- Improve type inference around procedural types.
- Handle casts involving classes and interfaces in `PlatformDependentCastRule`.
- Exclude test code in `TooManyArgumentsRule`.
- Exclude test code in `TooManyVariablesRule`.
- Update rule description for `SwallowedExceptionsRule`.
- Update rule description for `DuplicatesRule`.

## [0.30.1] - 2021-04-06

### Changed

- Improve type inference around array constant expressions.

### Fixed

- Fix overload resolution regressions caused by the new bounds checking.

## [0.30.0] - 2021-04-06

### Added

- Support for inline vars:
  - See: [Introducing Inline Variables in the Delphi Language](https://blog.marcocantu.com/blog/2018-october-inline-variables-delphi.html)
- Support for XE7 dynamic array operations:
  - See: [Dynamic Arrays in XE7](https://blog.marcocantu.com/blog/2014_september_dynamic_arrays_delphixe7.html)
- Support for `SizeOf` intrinsic in preprocessor expressions.
- Support for obscure `(.` and `.)` tokens:
  - These are now interpreted correctly by the lexer as square brackets.
  - See: [The Future of the Delphi Compiler](https://web.archive.org/web/20210330140547/https://edn.embarcadero.com/article/39174)
- `InlineVarExplicitTypeRule` analysis rule, which flags inline `var` declarations that don't
  specify a type.
- `InlineConstExplicitTypeRule` analysis rule, which flags inline `const` declarations that don't
  specify a type.
- `InlineLoopVarExplicitTypeRule` analysis rule, which flags inline loop `var` declarations that
  don't specify a type.
- `InlineDeclarationCapturedByAnonymousMethodRule` analysis rule, which flags places where inline
  declarations are captured by anonymous methods.

### Changed

- Improve handling of cast expressions in `MemoryManagementRule`.
- Improve overload resolution in cases with subrange bounds violations.
- Improve primary expression type resolution around constructors.
- Update various rule descriptions.

### Fixed

- Treat the `Exit` intrinsic's parameter as optional.
- Make preprocessor defines case-insensitive.
- FPs around casts from class reference to class in `RedundantCastRule`.

## [0.29.0] - 2021-03-23

### Added

- `NoSonarRule` analysis rule, which flags usages of the `NOSONAR` marker.
- `PlatformDependentCastRule` analysis rule, which flags casts between platform-dependent and
  platform-independent types.

### Changed

- Improve handling of cast expressions in `MemoryManagementRule`.
- Change `memoryFunctions` parameter to fully-qualified names in `MemoryManagementRule`.
- Remove `MemoryManagementRule` from the Sonar Way quality profile.
- Improve handling of constructor calls in `RedundantCastRule`.
- Improve handling of multi-variable declarations in `TooManyVariablesRule`.
- Improve handling of `else` blocks (within `case` statements and `except` blocks) in
  `BeginEndRequiredRule`.

## [0.28.0] - 2021-02-02

### Added

- Support for toolchain-aware-analysis.
- Support for the `SizeOf` intrinsic in constant expressions.
- `sonar.delphi.compiler.toolchain` property to specify the compiler toolchain used by the project.
- `sonar.delphi.compiler.version` property to specify the compiler version used by the project.

### Changed

- Improve handling of files with differing encodings:
  - If a file is not being analyzed, but is simply in the search path or standard library, the
    encoding will be detected by the BOM (or lack thereof).
  - [This encoding detection behavior matches the Delphi compiler](https://blogs.embarcadero.com/the-delphi-compiler-and-utf-8-encoded-source-code-files-with-no-bom/).
  - Encoding detection will not be used for files being analyzed by Sonar, which must continue to
    match the `sonar.sourceEncoding` property. The Sonar API does not support analysis of projects
    with mixed encodings.
- Improve type comparisons between `AnsiString` types with different code pages.

### Fixed

- Allow custom attributes after parameter specifiers.
- Fix type comparison issues between `PChar` and array of `Char`.
- Fix type comparison issues between `PChar` and `String`.
- Remove `FixedInt` and `FixedUInt` intrinsic type definitions, as they're actually real type
  aliases defined in `System`.
- Remove `Flush` intrinsic method definition, as it's actually a real method defined in `System`.

## [0.27.0] - 2020-11-26

### Added

- Configurable `prefixes` parameter for the various naming convention rules.

### Changed

- Exclude `System.Assigned` arguments in `EmptyBracketsRule`.

### Fixed

- Preprocessor includes referenced via absolute paths would not resolve correctly.
- Obscure `ClassCastException` during name resolution.

## [0.26.0] - 2020-10-15

### Added

- Support for variant record tags in semantic analysis.
- `RedundantAssignmentRule` analysis rule, which flags assignments where the left and right side are
  the same.
- `ForbiddenImportFilePatternRule` analysis rule template, which flags imports matching a specified
  file pattern.

### Changed

- Improve dependency analysis around `inherited` expressions.
- Exclude methods implementing interfaces in `EmptyMethodRule`.
- Exclude cast expressions where both operands have unknown types in `RedundantCastRule`.
- Exclude record variant sections in `NoSemiAfterFieldDeclarationRule`.
- Allow `Sorted := True` to be anywhere in the same block (instead of only on adjacent lines) in
  `DuplicatesRule`.
- Improve detection of "unsupported operation" methods in `MethodResultAssignedRule`.

### Fixed

- Ambiguous type comparisons from array constructors to sets.
- Subranges were not always considered convertible to their host type.
- Name resolution wasn't occurring for arguments in explicit array constructor invocations.
- The `AtomicCmpExchange` intrinsic signature was missing the `Target` parameter.
- Token indices would be get out of sync in files with preprocessor includes.

## [0.25.0] - 2020-09-01

### Added

- `ForbiddenPropertyRule` analysis rule template, which flags properties with a specified
  fully-qualified name.

### Changed

- Improve the symbol table's memory efficiency.

## [0.24.0] - 2020-08-27

### Added

- `scope` property for all rule templates.

## [0.23.1] - 2020-08-25

### Added

- Support for `ReturnAddress` intrinsic.
- Support for `AddressOfReturnAddress` intrinsic.

### Changed

- Update issue message and rule description for `UnusedImportsRule`.

## [0.23.0] - 2020-08-07

### Added

- Support for pascal-style result assignments in semantic analysis.
- `ObjectInvokedConstructorRule` analysis rule, which flags constructor calls on object instances.
- `MethodResultAssignedRule` analysis rule, which flags methods where `Result` or `out` parameters
  have not been assigned.
- `PascalStyleResultRule` analysis rule, which flags cases where a function result is assigned to
  the function name instead of `Result`.
- `NoSemiAfterFieldDeclarationRule` analysis rule, which flags field declarations without trailing
  semicolons.
- `TrailingWhitespaceRule` analysis rule, which flags source code lines with trailing whitespace.
- `IfThenShortCircuitRule` analysis rule, which flags erroneous `IfThen` usages that may cause
  access violations.
- `GotoStatementRule` analysis rule, which flags `goto` statements.
- `CompilerHintsRule` analysis rule, which flags places where hints are disabled.

### Changed

- Improve support for the `SetString` intrinsic.
- Exclude class declarations with no members in `ClassPerFileRule`.
- Exclude constructor invocations on instance variables in `MemoryManagementRule`.

## [0.22.1] - 2020-08-03

### Changed

- Improve dependency analysis around implicit invocations by `for`..`in` loops.
- Update default `severity` of `RedundantCastRule` to `MINOR`.
- Update default `severity` of `ExplicitDefaultPropertyReferenceRule` to `MINOR`.

### Fixed

- Loop variables were being skipped during name resolution.

## [0.22.0] - 2020-07-29

### Added

- `ExplicitDefaultPropertyReferenceRule` analysis rule, which flags direct references to `default`
  properties.
- `RedundantCastRule` analysis rule, which flags casts that can be removed.

### Fixed

- Type-modeling bugs around single-dimensional fixed array types.
- The `SetLength` wouldn't accept variadic length parameters for multi-dimensional arrays.
- `Self` in class methods is now treated as a class-reference instead of an instance variable.
- `Self` is no longer created in class methods with the `static` directive.

## [0.21.0] - 2020-06-10

### Added

- Support for operator overloads.
- Support for pointer math expressions.
- Support inline methods expanded via property invocations in dependency analysis.
- Support for `with` statements.
- Support for `System.MaxInt` compiler constant.
- Support for `System.MaxLongInt` compiler constant.
- Support for `System.CompilerVersion` compiler constant.
- Support for `Default` intrinsic.
- Support for `HasWeakRef` intrinsic.
- Support for `IsConstValue` intrinsic.
- Support for `IsManagedType` intrinsic.
- Support for implicit import of the `SysInit` unit
- Support for implicit conversion from interface references to `System.TGUID`.

### Changed

- Rewrite type resolution for binary and unary expressions.
- Support the `Result` variable in anonymous functions.
- Improve type resolution for array constructors with procedural elements.
- Improve type comparisons between ordinals and subranges with comparable base types.
- Improve type comparisons between variants and enumerations.
- Improve type comparisons between variants and dynamic arrays.
- Improve type comparisons between character pointers and strings.
- Improve type comparisons between character arrays and strings.
- Improve type comparisons between open arrays of `AnsiChar` and `WideChar`.
- Improve type comparisons between text-literal arguments and character pointer parameters.
- Improve type comparisons involving `nil` literals.
- Improve support for the `High` and `Low` intrinsics.
- Improve primary expression type resolution around constructor invocations.
- Improve primary expression type resolution around hard casts.
- Improve primary expression type resolution around array properties.
- Types nested within the `testSuiteType` will now be treated as test code.
- `Char` is now treated as an alias to `WideChar`.

### Fixed

- Name resolution bug where names in interface supertypes could not be resolved.
- Incorrect operator precedence for `as`.
- A caught exception's type was unresolved within the `except` handler.
- Exclude implementation symbols during cross-unit name resolution.
- Type signature mismatches arising from forward-declared pointer/class reference types.
- Anonymous methods were erroneously treated like method references during overload resolution.
- Nested types could not access declarations in top-level type(s) via unqualified references.
- A method implementation with no parameter list would not resolve to the interface method if the
  parameter list was omitted.
  (The compiler allows this as long as there is only one method declaration with the same name in
  the interface section)
- The `Result` variable's type would be unknown if the return type was omitted on the method
  implementation.
- References to `SetString` intrinsic wouldn't resolve properly.
- References to `Copy` intrinsic wouldn't resolve properly if optional parameters were omitted.
- The `Hi` intrinsic's return type was treated as `Integer`, when it should have been `Byte`.
- The `Length` intrinsic would only accept dynamic arrays, when it should accept any array.
- The `Include` and `Exclude` intrinsics wouldn't accept array constructors as set literals.
- Many intrinsics wouldn't treat subranges or `type` types as ordinals.
- In some cases, the current type could not be found during name resolution. This would cause an
  otherwise-correct declaration to be considered inaccessible.
- `nil` was treated as a valid record value.
- Record helpers would not be resolved properly on array types.
- Record helpers would not be resolved properly on intrinsic type references.
- Type resolution failures caused by mixing hard typecasts with types referred to via a keyword
  (ie. `string` or `file`).
- Ambiguous conversions from integer types to nil pointer.
- Valid conversions to variant parameters would not always succeed, or would be given a lower
  priority than expected during overload resolution.
- Array arguments could not convert to a pointer parameter with its element type.
- Generic declarations could be shadowed by non-generic declarations of the same name, erroneously
  ignoring type arguments.
- Overload resolution ambiguity issues between types with similar inheritance depths.
- Various generic specialization issues.

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

## 0.1.0 - 2019-07-29

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

[Unreleased]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.17.2...HEAD
[1.17.2]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.17.1...v1.17.2
[1.17.1]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.17.0...v1.17.1
[1.17.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.16.0...v1.17.0
[1.16.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.15.0...v1.16.0
[1.15.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.14.1...v1.15.0
[1.14.1]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.14.0...v1.14.1
[1.14.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.13.0...v1.14.0
[1.13.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.12.2...v1.13.0
[1.12.2]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.12.1...v1.12.2
[1.12.1]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.12.0...v1.12.1
[1.12.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.11.0...v1.12.0
[1.11.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.10.0...v1.11.0
[1.10.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.9.0...v1.10.0
[1.9.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.8.0...v1.9.0
[1.8.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.7.0...v1.8.0
[1.7.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.6.0...v1.7.0
[1.6.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.5.0...v1.6.0
[1.5.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.40.0...v1.0.0
[0.40.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.39.1...v0.40.0
[0.39.1]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.39.0...v0.39.1
[0.39.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.38.0...v0.39.0
[0.38.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.37.1...v0.38.0
[0.37.1]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.37.0...v0.37.1
[0.37.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.36.0...v0.37.0
[0.36.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.35.0...v0.36.0
[0.35.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.34.2...v0.35.0
[0.34.2]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.34.1...v0.34.2
[0.34.1]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.34.0...v0.34.1
[0.34.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.33.0...v0.34.0
[0.33.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.32.0...v0.33.0
[0.32.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.31.1...v0.32.0
[0.31.1]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.31.0...v0.31.1
[0.31.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.30.1...v0.31.0
[0.30.1]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.30.0...v0.30.1
[0.30.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.29.0...v0.30.0
[0.29.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.28.0...v0.29.0
[0.28.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.27.0...v0.28.0
[0.27.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.26.0...v0.27.0
[0.26.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.25.0...v0.26.0
[0.25.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.24.0...v0.25.0
[0.24.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.23.1...v0.24.0
[0.23.1]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.23.0...v0.23.1
[0.23.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.22.1...v0.23.0
[0.22.1]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.22.0...v0.22.1
[0.22.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.21.0...v0.22.0
[0.21.0]: https://github.com/integrated-application-development/sonar-delphi/compare/v0.20.0...v0.21.0
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
