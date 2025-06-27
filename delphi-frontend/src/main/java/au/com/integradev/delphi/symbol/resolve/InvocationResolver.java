/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.symbol.resolve;

import static au.com.integradev.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_1;
import static au.com.integradev.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_2;
import static au.com.integradev.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_3;
import static au.com.integradev.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_7;
import static au.com.integradev.delphi.symbol.resolve.EqualityType.EQUAL;
import static au.com.integradev.delphi.symbol.resolve.EqualityType.EXACT;
import static au.com.integradev.delphi.symbol.resolve.EqualityType.INCOMPATIBLE_TYPES;
import static java.lang.Math.abs;
import static java.util.function.Predicate.not;

import au.com.integradev.delphi.symbol.resolve.TypeConverter.TypeConversion;
import au.com.integradev.delphi.type.TypeUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.api.type.CodePages;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.AnsiStringType;
import org.sonar.plugins.communitydelphi.api.type.Type.CollectionType;
import org.sonar.plugins.communitydelphi.api.type.Type.FileType;
import org.sonar.plugins.communitydelphi.api.type.Type.IntegerType;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType.ProceduralKind;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;

/**
 * Resolves an invocation to the correct declaration. Based directly off of the tcallcandidates
 * class from the FreePascal compiler
 *
 * @see <a href="https://github.com/fpc/FPCSource/blob/main/compiler/htypechk.pas#L72">
 *     tcallcandidates</a>
 */
public class InvocationResolver {
  private final List<InvocationCandidate> candidates;
  private final List<InvocationArgument> arguments;

  public InvocationResolver() {
    this.candidates = new ArrayList<>();
    this.arguments = new ArrayList<>();
  }

  public void addCandidate(InvocationCandidate candidate) {
    candidates.add(candidate);
  }

  public void addArgument(InvocationArgument argument) {
    arguments.add(argument);
  }

  public List<InvocationArgument> getArguments() {
    return arguments;
  }

  /**
   * Processes the invocation candidates and builds up information that we can use for picking the
   * best one.
   *
   * @see <a href="https://github.com/fpc/FPCSource/blob/main/compiler/htypechk.pas#L2884">
   *     tcallcandidates.get_information</a>
   */
  public void processCandidates() {
    for (InvocationCandidate candidate : candidates) {
      for (int i = 0; i < arguments.size(); ++i) {
        InvocationArgument argument = arguments.get(i);
        Parameter parameter = candidate.getData().getParameter(i);
        processArgument(candidate, argument, parameter);

        if (candidate.isInvalid()) {
          break;
        }
      }
    }
  }

  /**
   * Checks a particular argument against the invocation candidate parameter.
   *
   * @see <a href="https://github.com/fpc/FPCSource/blob/main/compiler/htypechk.pas#L2884">
   *     tcallcandidates.get_information</a>
   */
  private static void processArgument(
      InvocationCandidate candidate, InvocationArgument argument, Parameter parameter) {
    Type argumentType = argument.getType();
    Type parameterType = parameter.getType();
    boolean ambiguousProceduralReference = false;

    // Convert ProceduralType to its returnType when not expecting a procedural type
    if (argumentType.isProcedural() && !parameterType.isProcedural()) {
      ProceduralType proceduralType = ((ProceduralType) argumentType);
      if (proceduralType.kind() != ProceduralKind.ANONYMOUS) {
        argumentType = proceduralType.returnType();
        ambiguousProceduralReference = argument.looksLikeProceduralReference();
      }
    }

    // If the parameter expects a procedural type then we need to find the overload that the
    // argument is referring to.
    if (argument.isRoutineReference(parameterType)) {
      argumentType = argument.findRoutineReferenceType(parameterType);
    }

    EqualityType equality = TypeComparer.compare(argumentType, parameterType);

    if (equality != EXACT) {
      if (equality == INCOMPATIBLE_TYPES) {
        TypeConversion conversion = TypeConverter.convert(argumentType, parameterType);

        if (conversion.isSuccessful()) {
          argumentType = conversion.getFrom();
          parameterType = conversion.getTo();
          equality = conversion.getEquality();
        }

        switch (conversion.getSource()) {
          case FROM:
            candidate.incrementImplicitConversionFromCount();
            break;
          case TO:
            candidate.incrementImplicitConversionToCount();
            break;
          default:
            // Do nothing
        }
      }

      if (equality.ordinal() < EQUAL.ordinal() && equalTypeRequired(parameter)) {
        // Parameter requires an equal type so the previous match was not good enough
        equality = varParameterAllowed(argumentType, parameter);
      }

      if (argumentType.isInteger()
          && parameterType.isPointer()
          && !argument.isImplicitlyConvertibleToNilPointer()) {
        equality = INCOMPATIBLE_TYPES;
      }

      if (argument.violatesBounds(parameterType)) {
        equality = INCOMPATIBLE_TYPES;
      }

      if (!equalTypeRequired(parameter)) {
        checkIntegerDistance(candidate, argumentType, parameterType);
        checkRealDistance(candidate, argumentType, parameterType);
        checkNumericMismatch(candidate, argumentType, parameterType);
        checkStructTypes(candidate, argumentType, parameterType);
        checkProceduralDistance(candidate, argumentType, parameterType);
      }
    }

    checkCodePageDistance(candidate, argumentType, parameterType);
    addVariantConversion(candidate, argumentType, parameterType);

    // When an ambiguous procedural type was changed to an invocation, an exact match is
    // downgraded to equal.
    // Ordinal distance is also increased.
    // This way an overload call with the procedural type is always chosen instead.
    if (equality == EXACT && ambiguousProceduralReference) {
      equality = EQUAL;
      candidate.increaseOrdinalDistance(1);
    }

    switch (equality) {
      case EXACT:
        candidate.incrementExactCount();
        break;
      case EQUAL:
        candidate.incrementEqualCount();
        break;
      case CONVERT_LEVEL_1:
        candidate.incrementConvertLevelCount(1);
        break;
      case CONVERT_LEVEL_2:
        candidate.incrementConvertLevelCount(2);
        break;
      case CONVERT_LEVEL_3:
        candidate.incrementConvertLevelCount(3);
        break;
      case CONVERT_LEVEL_4:
        candidate.incrementConvertLevelCount(4);
        break;
      case CONVERT_LEVEL_5:
        candidate.incrementConvertLevelCount(5);
        break;
      case CONVERT_LEVEL_6:
        candidate.incrementConvertLevelCount(6);
        break;
      case CONVERT_LEVEL_7:
        candidate.incrementConvertLevelCount(7);
        break;
      case CONVERT_LEVEL_8:
        candidate.incrementConvertLevelCount(8);
        break;
      case INCOMPATIBLE_TYPES:
        candidate.setInvalid();
        break;
      default:
        // Do nothing
    }
  }

  private static void checkIntegerDistance(
      InvocationCandidate candidate, Type argumentType, Type parameterType) {
    if (argumentType.isInteger() && parameterType.isInteger()) {
      IntegerType argInteger = (IntegerType) argumentType;
      IntegerType paramInteger = (IntegerType) parameterType;
      double distance = argInteger.max().subtract(paramInteger.max()).abs().doubleValue();

      candidate.increaseOrdinalDistance(distance);

      if (argInteger.isSigned() != paramInteger.isSigned()) {
        candidate.incrementSignMismatchCount();
      }
    }
  }

  private static void checkRealDistance(
      InvocationCandidate candidate, Type argumentType, Type parameterType) {
    if (argumentType.isReal() && parameterType.isReal()) {
      int argumentSize = argumentType.size();
      int parameterSize = parameterType.size();
      int distance;
      if (argumentSize > parameterSize) {
        // Penalty for shrinking of precision
        distance = (argumentSize - parameterSize) * 16;
      } else {
        distance = parameterSize - argumentSize;
      }
      candidate.increaseOrdinalDistance(distance);
    }
  }

  private static void checkNumericMismatch(
      InvocationCandidate candidate, Type argumentType, Type parameterType) {
    if (argumentType.isInteger() && parameterType.isReal()) {
      candidate.incrementNumericMismatchCount();
    }
  }

  private static void checkStructTypes(
      InvocationCandidate candidate, Type argumentType, Type parameterType) {
    if (argumentType.isStruct() && parameterType.isStruct()) {
      StructType from = (StructType) argumentType;
      StructType to = (StructType) parameterType;
      if (from.kind() != to.kind()) {
        candidate.incrementStructMismatchCount();
      }
      candidate.increaseOrdinalDistance(calculateStructDistance(from, to));
    }
  }

  private static int calculateStructDistance(Type from, Type to) {
    int result = 0;
    while (from.isDescendantOf(to)) {
      ++result;
      from = from.parent();
    }
    return result;
  }

  private static void checkProceduralDistance(
      InvocationCandidate candidate, Type argumentType, Type parameterType) {
    if (argumentType.isProcedural() && parameterType.isProcedural()) {
      ProceduralKind argKind = ((ProceduralType) argumentType).kind();
      ProceduralKind paramKind = ((ProceduralType) parameterType).kind();
      int kindDistance = abs(argKind.ordinal() - paramKind.ordinal());

      candidate.increaseProceduralDistance(kindDistance);
    }

    if (argumentType.isPointer() && parameterType.isProcedural()) {
      candidate.increaseProceduralDistance(((ProceduralType) parameterType).kind().ordinal());
    }
  }

  private static void checkCodePageDistance(
      InvocationCandidate candidate, Type argumentType, Type parameterType) {
    if (!parameterType.isAnsiString()) {
      return;
    }

    Integer codePage = null;

    if (argumentType.isAnsiString()) {
      codePage = ((AnsiStringType) argumentType).codePage();
    } else if (TypeUtils.findBaseType(argumentType).is(IntrinsicType.PANSICHAR)) {
      codePage = CodePages.CP_ACP;
    }

    if (codePage == null) {
      return;
    }

    AnsiStringType to = (AnsiStringType) parameterType;
    if (codePage == to.codePage() || to.codePage() == CodePages.CP_NONE) {
      return;
    }

    switch (to.codePage()) {
      case CodePages.CP_UTF8:
        candidate.increaseCodePageDistance(1);
        break;
      case CodePages.CP_ACP:
        candidate.increaseCodePageDistance(2);
        break;
      default:
        candidate.increaseCodePageDistance(3);
        break;
    }
  }

  private static void addVariantConversion(
      InvocationCandidate candidate, Type argumentType, Type parameterType) {
    if (argumentType.isVariant() && !parameterType.isVariant()) {
      candidate.addVariantConversion(parameterType);
    } else if (parameterType.isVariant() && !argumentType.isVariant()) {
      candidate.addVariantConversion(argumentType);
    } else {
      candidate.addVariantConversion(null);
    }
  }

  private static EqualityType varParameterAllowed(Type argType, Parameter parameter) {
    Type paramType = parameter.getType();

    if (paramType.isUntyped() && !parameter.isConst()) {
      return CONVERT_LEVEL_7;
    }

    if (paramType.isOpenArray()) {
      if (argType.isDynamicArray()
          && TypeComparer.equals(
              ((CollectionType) argType).elementType(),
              ((CollectionType) paramType).elementType())) {
        return CONVERT_LEVEL_2;
      } else if (TypeComparer.equals(argType, ((CollectionType) paramType).elementType())) {
        return CONVERT_LEVEL_3;
      }
    }

    if (paramType.isPointer() && argType.isPointer()) {
      // An implicit pointer conversion is allowed
      return CONVERT_LEVEL_1;
    }

    if (paramType.isFile()
        && argType.isFile()
        && !((FileType) argType).fileType().isUntyped()
        && ((FileType) paramType).fileType().isUntyped()) {
      // An implicit file conversion is allowed from a typed file to an untyped one
      return CONVERT_LEVEL_1;
    }

    return INCOMPATIBLE_TYPES;
  }

  private static boolean equalTypeRequired(Parameter parameter) {
    return parameter.isOut() || parameter.isVar();
  }

  /**
   * Tries to choose the best candidate based on the invocation arguments. Losing candidates are
   * marked as invalid.
   *
   * @return The remaining candidates
   * @see <a href="https://github.com/fpc/FPCSource/blob/main/compiler/htypechk.pas#L3511">
   *     tcallcandidates.choose_best</a>
   */
  public Set<InvocationCandidate> chooseBest() {
    if (candidates.isEmpty()) {
      return Collections.emptySet();
    }

    InvocationCandidate bestCandidate = candidates.get(0);

    for (int i = 1; i < candidates.size(); ++i) {
      InvocationCandidate candidate = candidates.get(i);
      if (candidate.isInvalid()) {
        // If it's invalid then it can't possibly be a better candidate.
        continue;
      }

      int result = isBetterCandidate(candidate, bestCandidate);

      if (result > 0) {
        // Current candidate is better, flag all previous candidates as incompatible
        for (int ii = 0; ii < i; ++ii) {
          candidates.get(ii).setInvalid();
        }
        bestCandidate = candidate;
      } else if (result < 0) {
        // bestCandidate is better, flag current candidate as incompatible
        candidate.setInvalid();
      }
    }

    return candidates.stream()
        .filter(not(InvocationCandidate::isInvalid))
        .collect(Collectors.toSet());
  }

  /**
   * To choose the best candidate we use the following order:
   *
   * <ul>
   *   <li>Invalid flag
   *   <li>(Smaller) Number of implicit casts based on the argument type.
   *   <li>(Smaller) Number of numeric mismatches.
   *   <li>(Smaller) Number of convertLevel[8..1] parameters.
   *   <li>(Bigger) Number of exact parameters.
   *   <li>(Smaller) Number of equal parameters.
   *   <li>(Smaller) Total of ordinal distance. For example, the distance of a word to a byte is
   *       65535-255=65280.
   *   <li>(Smaller) Number of struct-kind mismatches.
   *   <li>(Smaller) Total of procedural distance.
   *   <li>(Smaller) Number of implicit casts based on the parameter type.
   * </ul>
   *
   * @param candidate Candidate we're checking
   * @param bestCandidate The current best candidate
   * @return
   *     <ul>
   *       <li>> 0 when candidate is better than bestCandidate
   *       <li>< 0 when bestCandidate is better than candidate
   *       <li>= 0 when both are equal
   *     </ul>
   *
   * @see <a href="https://github.com/fpc/FPCSource/blob/main/compiler/htypechk.pas#L3258">
   *     is_better_candidate </a>
   */
  private int isBetterCandidate(InvocationCandidate candidate, InvocationCandidate bestCandidate) {
    Preconditions.checkState(!candidate.isInvalid());

    if (bestCandidate.isInvalid()) {
      return 1;
    }

    ComparisonChain comparisonChain =
        ComparisonChain.start()
            // Builtin operators will always lose to user-defined operator overloads
            .compareFalseFirst(bestCandidate.isOperatorIntrinsic(), candidate.isOperatorIntrinsic())
            // Builtin operators will always lose to variant operators
            .compareTrueFirst(bestCandidate.isVariantOperator(), candidate.isVariantOperator())
            // Less implicit conversions based on the argument type?
            .compare(
                bestCandidate.getImplicitConversionFromCount(),
                candidate.getImplicitConversionFromCount())
            // Less numeric mismatches?
            .compare(bestCandidate.getNumericMismatchCount(), candidate.getNumericMismatchCount());

    for (int i = InvocationCandidate.CONVERT_LEVELS; i > 0; --i) {
      // Less castLevel[8..1] parameters?
      comparisonChain =
          comparisonChain.compare(
              bestCandidate.getConvertLevelCount(i), candidate.getConvertLevelCount(i));
    }

    int result =
        comparisonChain
            // More exact parameters?
            .compare(candidate.getExactCount(), bestCandidate.getExactCount())
            // Less equal parameters?
            .compare(bestCandidate.getEqualCount(), candidate.getEqualCount())
            // Smaller ordinal distance?
            .compare(bestCandidate.getOrdinalDistance(), candidate.getOrdinalDistance())
            // Less sign mismatches?
            .compare(bestCandidate.getSignMismatchCount(), candidate.getSignMismatchCount())
            // Less struct mismatches?
            .compare(bestCandidate.getStructMismatchCount(), candidate.getStructMismatchCount())
            // Smaller procedural distance?
            .compare(bestCandidate.getProceduralDistance(), candidate.getProceduralDistance())
            // Smaller codePage distance?
            .compare(bestCandidate.getCodePageDistance(), candidate.getCodePageDistance())
            // Less implicit conversions based on the parameter type?
            .compare(
                bestCandidate.getImplicitConversionToCount(),
                candidate.getImplicitConversionToCount())
            .result();

    if (result == 0) {
      result = getVariantDistance(candidate, bestCandidate);
    }

    return result;
  }

  private int getVariantDistance(InvocationCandidate candidate, InvocationCandidate bestCandidate) {
    int variantDistance = 0;
    for (int i = 0; i < arguments.size(); ++i) {
      Type current = candidate.getVariantConversionType(i);
      Type best = bestCandidate.getVariantConversionType(i);
      variantDistance += isBetterVariantConversion(current, best);
    }
    return variantDistance;
  }

  private static int isBetterVariantConversion(Type current, Type best) {
    if (current == null && best == null) {
      return 0;
    }
    return ComparisonChain.start()
        .compare(current, best, Comparator.comparing(Objects::isNull))
        .compare(current, best, Comparator.comparing(InvocationResolver::isIInterface))
        .compare(current, best, Comparator.comparing(Type::isUntyped))
        .compare(current, best, InvocationResolver::compareNumericType)
        .compare(current, best, InvocationResolver::compareRealSize)
        .compare(current, best, InvocationResolver::compareIntegerRange)
        .compare(current, best, InvocationResolver::compareStringType)
        .result();
  }

  private static boolean isIInterface(Type type) {
    return TypeUtils.findBaseType(type).is("System.IInterface");
  }

  private static int compareNumericType(Type a, Type b) {
    if (a.isReal() && b.isInteger()) {
      return 1;
    } else if (b.isReal() && a.isInteger()) {
      return -1;
    } else {
      return 0;
    }
  }

  private static int compareRealSize(Type a, Type b) {
    if (!a.isReal() || !b.isReal()) {
      return 0;
    }
    if (isCurrencyCompConflict(a, b) || isCurrencyCompConflict(b, a)) {
      return 0;
    }
    return Objects.compare(a, b, Comparator.comparingInt(Type::size));
  }

  private static boolean isCurrencyCompConflict(Type currencyComp, Type real) {
    currencyComp = TypeUtils.findBaseType(currencyComp);
    return (currencyComp.is(IntrinsicType.CURRENCY) || currencyComp.is(IntrinsicType.COMP))
        && currencyComp.size() >= real.size();
  }

  private static int compareIntegerRange(Type a, Type b) {
    if (!a.isInteger() || !b.isInteger()) {
      return 0;
    }

    IntegerType intA = (IntegerType) a;
    IntegerType intB = (IntegerType) b;

    if (valueRangesAreAmbiguous(intA, intB)) {
      return 0;
    }

    return ComparisonChain.start()
        .compare(intA, intB, Comparator.comparing(IntegerType::max))
        .compare(intB, intA, Comparator.comparing(IntegerType::min))
        .result();
  }

  private static boolean valueRangesAreAmbiguous(IntegerType a, IntegerType b) {
    return a.isSigned() == b.isSigned()
        && !(a.min().compareTo(b.min()) <= 0 && a.max().compareTo(b.max()) >= 0)
        && !(b.min().compareTo(a.min()) <= 0 && b.max().compareTo(a.max()) >= 0);
  }

  private static int compareStringType(Type a, Type b) {
    if (!a.isString() || !b.isString()) {
      return 0;
    }

    return Comparator.<Type>comparingInt(
            type -> {
              type = TypeUtils.findBaseType(type);
              if (type.is(IntrinsicType.WIDESTRING)) {
                return 1;
              } else if (type.is(IntrinsicType.UNICODESTRING)) {
                return 2;
              } else if (type.isAnsiString()) {
                return 3;
              } else {
                return 4;
              }
            })
        .reversed()
        .compare(a, b);
  }
}
