/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.checks;

import java.util.List;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

public final class CheckList {
  public static final String REPOSITORY_KEY = "community-delphi";

  private static final List<Class<? extends DelphiCheck>> ALL_CHECKS =
      List.of(
          // Listed alphabetically
          AddressOfCharacterDataCheck.class,
          AddressOfNestedRoutineCheck.class,
          AssertMessageCheck.class,
          AssignedAndFreeCheck.class,
          AttributeNameCheck.class,
          BeginEndRequiredCheck.class,
          CaseStatementSizeCheck.class,
          CastAndFreeCheck.class,
          CatchingRawExceptionCheck.class,
          CharacterToCharacterPointerCastCheck.class,
          ClassNameCheck.class,
          ClassPerFileCheck.class,
          CognitiveComplexityRoutineCheck.class,
          CommentRegularExpressionCheck.class,
          CommentedOutCodeCheck.class,
          CompilerHintsCheck.class,
          CompilerWarningsCheck.class,
          ConsecutiveConstSectionCheck.class,
          ConsecutiveTypeSectionCheck.class,
          ConsecutiveVarSectionCheck.class,
          ConsecutiveVisibilitySectionCheck.class,
          ConstantNameCheck.class,
          ConstructorNameCheck.class,
          ConstructorWithoutInheritedCheck.class,
          CyclomaticComplexityRoutineCheck.class,
          DateFormatSettingsCheck.class,
          DestructorNameCheck.class,
          DestructorWithoutInheritedCheck.class,
          DigitGroupingCheck.class,
          DigitSeparatorCheck.class,
          EmptyArgumentListCheck.class,
          EmptyBlockCheck.class,
          EmptyFieldSectionCheck.class,
          EmptyFileCheck.class,
          EmptyFinallyBlockCheck.class,
          EmptyInterfaceCheck.class,
          EmptyRoutineCheck.class,
          EmptyVisibilitySectionCheck.class,
          EnumNameCheck.class,
          ExhaustiveEnumCaseCheck.class,
          ExplicitBitwiseNotCheck.class,
          ExplicitDefaultPropertyReferenceCheck.class,
          ExplicitTObjectInheritanceCheck.class,
          FieldNameCheck.class,
          ForbiddenConstantCheck.class,
          ForbiddenEnumValueCheck.class,
          ForbiddenFieldCheck.class,
          ForbiddenIdentifierCheck.class,
          ForbiddenImportFilePatternCheck.class,
          ForbiddenPropertyCheck.class,
          ForbiddenRoutineCheck.class,
          ForbiddenTypeCheck.class,
          FormDfmCheck.class,
          FormFmxCheck.class,
          FormatArgumentCountCheck.class,
          FormatArgumentTypeCheck.class,
          FormatStringValidCheck.class,
          FreeAndNilTObjectCheck.class,
          FullyQualifiedImportCheck.class,
          GotoStatementCheck.class,
          GroupedFieldDeclarationCheck.class,
          GroupedParameterDeclarationCheck.class,
          GroupedVariableDeclarationCheck.class,
          HelperNameCheck.class,
          IfThenShortCircuitCheck.class,
          ImplicitDefaultEncodingCheck.class,
          ImportSpecificityCheck.class,
          IndexLastListElementCheck.class,
          InheritedMethodWithNoCodeCheck.class,
          InheritedTypeNameCheck.class,
          InlineAssemblyCheck.class,
          InlineConstExplicitTypeCheck.class,
          InlineDeclarationCapturedByAnonymousMethodCheck.class,
          InlineLoopVarExplicitTypeCheck.class,
          InlineVarExplicitTypeCheck.class,
          InstanceInvokedConstructorCheck.class,
          InterfaceGuidCheck.class,
          InterfaceNameCheck.class,
          IterationPastHighBoundCheck.class,
          LegacyInitializationSectionCheck.class,
          LoopExecutingAtMostOnceCheck.class,
          LowercaseKeywordCheck.class,
          MathFunctionSingleOverloadCheck.class,
          MemberDeclarationOrderCheck.class,
          MissingRaiseCheck.class,
          MissingSemicolonCheck.class,
          MixedNamesCheck.class,
          NilComparisonCheck.class,
          NoSonarCheck.class,
          NonLinearCastCheck.class,
          ObjectTypeCheck.class,
          ParsingErrorCheck.class,
          PascalStyleResultCheck.class,
          PlatformDependentCastCheck.class,
          PlatformDependentTruncationCheck.class,
          PointerNameCheck.class,
          ProjectFileRoutineCheck.class,
          ProjectFileVariableCheck.class,
          PublicFieldCheck.class,
          RaisingRawExceptionCheck.class,
          ReRaiseExceptionCheck.class,
          RecordNameCheck.class,
          RedundantAssignmentCheck.class,
          RedundantBooleanCheck.class,
          RedundantCastCheck.class,
          RedundantInheritedCheck.class,
          RedundantJumpCheck.class,
          RedundantParenthesesCheck.class,
          RoutineNameCheck.class,
          RoutineNestingDepthCheck.class,
          RoutineResultAssignedCheck.class,
          ShortIdentifierCheck.class,
          StringListDuplicatesCheck.class,
          StringLiteralRegularExpressionCheck.class,
          SuperfluousSemicolonCheck.class,
          SwallowedExceptionCheck.class,
          TabulationCharacterCheck.class,
          TooLargeRoutineCheck.class,
          TooLongLineCheck.class,
          TooManyNestedRoutinesCheck.class,
          TooManyParametersCheck.class,
          TooManyVariablesCheck.class,
          TrailingCommaArgumentListCheck.class,
          TrailingWhitespaceCheck.class,
          TypeAliasCheck.class,
          UnicodeToAnsiCastCheck.class,
          UnitLevelKeywordIndentationCheck.class,
          UnitNameCheck.class,
          UnspecifiedReturnTypeCheck.class,
          UnusedConstantCheck.class,
          UnusedFieldCheck.class,
          UnusedGlobalVariableCheck.class,
          UnusedImportCheck.class,
          UnusedLocalVariableCheck.class,
          UnusedPropertyCheck.class,
          UnusedRoutineCheck.class,
          UnusedTypeCheck.class,
          VariableInitializationCheck.class,
          VariableNameCheck.class,
          VisibilityKeywordIndentationCheck.class,
          VisibilitySectionOrderCheck.class,
          WithStatementCheck.class);

  private CheckList() {
    // Utility class
  }

  public static List<Class<?>> getChecks() {
    return List.copyOf(ALL_CHECKS);
  }
}
