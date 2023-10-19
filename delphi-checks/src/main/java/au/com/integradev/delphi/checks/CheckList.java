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
          AddressOfSubroutineCheck.class,
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
          CognitiveComplexityMethodCheck.class,
          CommentRegularExpressionCheck.class,
          CommentedOutCodeCheck.class,
          CompilerHintsCheck.class,
          CompilerWarningsCheck.class,
          ConstantNameCheck.class,
          ConstructorNameCheck.class,
          ConstructorWithoutInheritedCheck.class,
          CyclomaticComplexityMethodCheck.class,
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
          EmptyMethodCheck.class,
          EmptyTypeSectionCheck.class,
          EmptyVisibilitySectionCheck.class,
          EnumNameCheck.class,
          ExplicitDefaultPropertyReferenceCheck.class,
          ExplicitTObjectInheritanceCheck.class,
          FieldNameCheck.class,
          ForbiddenConstantCheck.class,
          ForbiddenEnumValueCheck.class,
          ForbiddenFieldCheck.class,
          ForbiddenIdentifierCheck.class,
          ForbiddenImportFilePatternCheck.class,
          ForbiddenMethodCheck.class,
          ForbiddenPropertyCheck.class,
          ForbiddenTypeCheck.class,
          FreeAndNilTObjectCheck.class,
          GotoStatementCheck.class,
          GroupedFieldDeclarationCheck.class,
          GroupedParameterDeclarationCheck.class,
          GroupedVariableDeclarationCheck.class,
          HelperNameCheck.class,
          IfThenShortCircuitCheck.class,
          ImportSpecificityCheck.class,
          InheritedMethodWithNoCodeCheck.class,
          InheritedTypeNameCheck.class,
          InlineConstExplicitTypeCheck.class,
          InlineDeclarationCapturedByAnonymousMethodCheck.class,
          InlineLoopVarExplicitTypeCheck.class,
          InlineVarExplicitTypeCheck.class,
          InterfaceGuidCheck.class,
          InterfaceNameCheck.class,
          LegacyInitializationSectionCheck.class,
          LowercaseKeywordCheck.class,
          MathFunctionSingleOverloadCheck.class,
          MemberDeclarationOrderCheck.class,
          MethodNameCheck.class,
          MethodNestingDepthCheck.class,
          MethodResultAssignedCheck.class,
          MissingSemicolonCheck.class,
          MixedNamesCheck.class,
          NilComparisonCheck.class,
          NoSonarCheck.class,
          InstanceInvokedConstructorCheck.class,
          ObjectTypeCheck.class,
          PascalStyleResultCheck.class,
          PlatformDependentCastCheck.class,
          PlatformDependentTruncationCheck.class,
          PointerNameCheck.class,
          ProjectFileMethodCheck.class,
          ProjectFileVariableCheck.class,
          PublicFieldCheck.class,
          RaisingRawExceptionCheck.class,
          ReRaiseExceptionCheck.class,
          RecordNameCheck.class,
          RedundantAssignmentCheck.class,
          RedundantBooleanCheck.class,
          RedundantCastCheck.class,
          RedundantParenthesesCheck.class,
          ShortIdentifierCheck.class,
          StringListDuplicatesCheck.class,
          StringLiteralRegularExpressionCheck.class,
          SuperfluousSemicolonCheck.class,
          SwallowedExceptionCheck.class,
          TabulationCharacterCheck.class,
          TooLargeMethodCheck.class,
          TooLongLineCheck.class,
          TooManyParametersCheck.class,
          TooManySubroutinesCheck.class,
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
          UnusedMethodCheck.class,
          UnusedPropertyCheck.class,
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
