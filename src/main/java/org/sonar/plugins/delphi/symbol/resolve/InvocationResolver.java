package org.sonar.plugins.delphi.symbol.resolve;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Math.abs;
import static java.util.function.Predicate.not;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_1;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_2;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_3;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_7;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.EQUAL;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.EXACT;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.INCOMPATIBLE_TYPES;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.ANSISTRING;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.BYTE;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.CARDINAL;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.DOUBLE_CURRENCY;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.DYNAMIC_ARRAY;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.ENUM;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.EXTENDED;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.FORMAL_BOOLEAN;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.INCOMPATIBLE_VARIANT;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.INTEGER;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.NO_CONVERSION_REQUIRED;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.SHORTINT;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.SHORTSTRING;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.SINGLE;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.SMALLINT;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.UNICODESTRING;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.WIDESTRING;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.WORD;

import com.google.common.collect.ComparisonChain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.plugins.delphi.symbol.declaration.parameter.Parameter;
import org.sonar.plugins.delphi.symbol.resolve.TypeConverter.TypeConversion;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.CollectionType;
import org.sonar.plugins.delphi.type.Type.DecimalType;
import org.sonar.plugins.delphi.type.Type.FileType;
import org.sonar.plugins.delphi.type.Type.IntegerType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Type.ProceduralType.ProceduralKind;
import org.sonar.plugins.delphi.type.Type.StructType;
import org.sonar.plugins.delphi.type.TypeUtils;

/**
 * Resolves an invocation to the correct declaration. Based directly off of the tcallcandidates
 * class from the FreePascal compiler
 *
 * @see <a href="https://github.com/graemeg/freepascal/blob/master/compiler/htypechk.pas#L69">
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
   * @see <a href="https://github.com/graemeg/freepascal/blob/master/compiler/htypechk.pas#2858">
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
   * @see <a href="https://github.com/graemeg/freepascal/blob/master/compiler/htypechk.pas#2858">
   *     tcallcandidates.get_information</a>
   */
  private void processArgument(
      InvocationCandidate candidate, InvocationArgument argument, Parameter parameter) {
    Type argumentType = argument.getType();
    Type parameterType = parameter.getType();
    boolean ambiguousMethodReference = false;

    // Convert ProceduralType to its returnType when not expecting a procedural type
    if (argumentType.isProcedural() && !parameterType.isProcedural()) {
      ProceduralType proceduralType = ((ProceduralType) argumentType);
      if (proceduralType.kind() != ProceduralKind.ANONYMOUS) {
        argumentType = proceduralType.returnType();
        ambiguousMethodReference = argument.looksLikeMethodReference();
      }
    }

    // If the parameter expects a procedural type then we need to find the overload that the
    // argument is referring to.
    if (argument.isMethodReference(parameterType)) {
      argumentType = argument.findMethodReferenceType(parameterType);
    }

    EqualityType equality = TypeComparer.compare(argumentType, parameterType);

    if (equality != EXACT) {
      if (equality == INCOMPATIBLE_TYPES) {
        TypeConversion conversion = TypeConverter.convert(argumentType, parameterType);
        if (conversion.isSuccessful()) {
          argumentType = conversion.getFrom();
          parameterType = conversion.getTo();
          equality = conversion.getEquality();
          candidate.incrementConvertOperatorCount();
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

      argumentType = TypeUtils.findBaseType(argumentType);
      parameterType = TypeUtils.findBaseType(parameterType);

      if (!equalTypeRequired(parameter)) {
        checkIntegerDistance(candidate, argumentType, parameterType);
        checkDecimalDistance(candidate, argumentType, parameterType);
        checkStructTypes(candidate, argumentType, parameterType);
        checkProceduralDistance(candidate, argumentType, parameterType);
      }
    }

    checkVariantConversions(candidate, argumentType, parameterType);

    // When an ambiguous procedural type was changed to an invocation, an exact match is
    // downgraded to equal.
    // Ordinal distance is also increased.
    // This way an overload call with the procedural type is always chosen instead.
    if (equality == EXACT && ambiguousMethodReference) {
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

      candidate.increaseOrdinalDistance(argInteger.ordinalDistance(paramInteger));

      if (argInteger.isSigned() != paramInteger.isSigned()) {
        candidate.incrementSignMismatchCount();
      }
    }
  }

  private static void checkDecimalDistance(
      InvocationCandidate candidate, Type argumentType, Type parameterType) {
    if (argumentType.isDecimal() && parameterType.isDecimal()) {
      int argumentSize = ((DecimalType) argumentType).size();
      int parameterSize = ((DecimalType) parameterType).size();
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
    while (from.isSubTypeOf(to)) {
      ++result;
      from = from.superType();
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

  // Keep track of implicit variant conversions
  // Also invalidate candidates that would produce invalid variant conversions
  private static void checkVariantConversions(
      InvocationCandidate candidate, Type argumentType, Type parameterType) {
    VariantConversionType variantConversionType = NO_CONVERSION_REQUIRED;
    if (argumentType.isVariant()) {
      variantConversionType = VariantConversionType.fromType(parameterType);
      if (variantConversionType == INCOMPATIBLE_VARIANT) {
        candidate.setInvalid();
      }
    } else if (parameterType.isVariant()) {
      variantConversionType = VariantConversionType.fromType(argumentType);
    }
    candidate.addVariantConversion(variantConversionType);
  }

  private EqualityType varParameterAllowed(Type argType, Parameter parameter) {
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
   * @see <a href="https://github.com/graemeg/freepascal/blob/master/compiler/htypechk.pas#3480">
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
   *   <li>(Smaller) Number of convert operator parameters.
   *   <li>(Smaller) Number of convertLevel[6..1] parameters.
   *   <li>(Bigger) Number of exact parameters.
   *   <li>(Smaller) Number of equal parameters.
   *   <li>(Smaller) Total of ordinal distance. For example, the distance of a word to a byte is
   *       65535-255=65280.
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
   * @see <a href="https://github.com/graemeg/freepascal/blob/master/compiler/htypechk.pas#L3230" />
   */
  private int isBetterCandidate(InvocationCandidate candidate, InvocationCandidate bestCandidate) {
    checkState(!candidate.isInvalid());

    if (bestCandidate.isInvalid()) {
      return 1;
    }

    ComparisonChain comparisonChain =
        ComparisonChain.start()
            // Builtin operators will always lose to user-defined operator overloads
            .compareFalseFirst(bestCandidate.isOperatorIntrinsic(), candidate.isOperatorIntrinsic())
            // Builtin operators will always lose to variant operators
            .compareTrueFirst(bestCandidate.isVariantOperator(), candidate.isVariantOperator())
            // Less Implicit operator arguments?
            .compare(bestCandidate.getConvertOperatorCount(), candidate.getConvertOperatorCount());

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
            .result();

    if (result == 0) {
      result = getVariantDistance(candidate, bestCandidate);
    }

    return result;
  }

  private int getVariantDistance(InvocationCandidate candidate, InvocationCandidate bestCandidate) {
    int variantDistance = 0;
    for (int i = 0; i < arguments.size(); ++i) {
      VariantConversionType currentVcl = candidate.getVariantConversionType(i);
      VariantConversionType bestVcl = bestCandidate.getVariantConversionType(i);
      variantDistance += isBetterVariantConversion(currentVcl, bestVcl);
    }
    return variantDistance;
  }

  /**
   * Determines which variant conversion type takes precedence when converting a variant type
   * argument to a parameter type.
   *
   * <p>Delphi precedence rules extracted from test programs:
   *
   * <ul>
   *   <li>single > (char, currency, int64, shortstring, ansistring, widestring, unicodestring,
   *       extended, double)
   *   <li>double/currency > (char, int64, shortstring, ansistring, widestring, unicodestring,
   *       extended)
   *   <li>extended > (char, int64, shortstring, ansistring, widestring, unicodestring)
   *   <li>longint/cardinal > (int64, shortstring, ansistring, widestring, unicodestring, extended,
   *       double, single, char, currency)
   *   <li>smallint > (longint, int64, shortstring, ansistring, widestring, unicodestring, extended,
   *       double single, char, currency);
   *   <li>word > (longint, cardinal, int64, shortstring, ansistring, widestring, unicodestring,
   *       extended, double single, char, currency);
   *   <li>shortint > (longint, smallint, int64, shortstring, ansistring, widestring, unicodestring,
   *       extended, double, single, char, currency)
   *   <li>byte > (longint, cardinal, word, smallint, int64, shortstring, ansistring, widestring,
   *       unicodestring, extended, double, single, char, currency);
   *   <li>boolean/formal > (char, int64, shortstring, ansistring, widestring, unicodestring)
   *   <li>widestring > (char, int64, shortstring, ansistring, unicodestring)
   *   <li>unicodestring > (char, int64, shortstring, ansistring)
   *   <li>ansistring > (char, int64, shortstring)
   *   <li>shortstring > (char, int64)
   * </ul>
   *
   * Relations not mentioned mean that they conflict: no decision possible
   *
   * @param currentVcl The conversion type we're checking
   * @param bestVcl The best conversion type so far
   * @return
   *     <ul>
   *       <li>> 0 when currentVcl is better than bestVcl
   *       <li>< 0 when bestVcl is better than currentVcl
   *       <li>= 0 when both are equal
   *     </ul>
   *
   * @see <a href="https://github.com/graemeg/freepascal/blob/master/compiler/htypechk.pas#L3339">
   *     is_better_candidate_single_variant</a>
   */
  private int isBetterVariantConversion(
      VariantConversionType currentVcl, VariantConversionType bestVcl) {
    if (currentVcl == bestVcl) {
      return 0;
    } else if (currentVcl == INCOMPATIBLE_VARIANT || bestVcl == NO_CONVERSION_REQUIRED) {
      return -1;
    } else if (bestVcl == INCOMPATIBLE_VARIANT || currentVcl == NO_CONVERSION_REQUIRED) {
      return 1;
    } else if (currentVcl == FORMAL_BOOLEAN || bestVcl == FORMAL_BOOLEAN) {
      if (currentVcl == FORMAL_BOOLEAN) {
        return VariantConversionType.isChari64Str(bestVcl) ? 1 : 0;
      } else {
        return VariantConversionType.isChari64Str(currentVcl) ? -1 : 0;
      }
    } else if (currentVcl == BYTE || bestVcl == BYTE) {
      return calculateRelation(currentVcl, bestVcl, BYTE, Set.of(SHORTINT));
    } else if (currentVcl == SHORTINT || bestVcl == SHORTINT) {
      return calculateRelation(currentVcl, bestVcl, SHORTINT, Set.of(WORD, CARDINAL));
    } else if (currentVcl == WORD || bestVcl == WORD) {
      return calculateRelation(currentVcl, bestVcl, WORD, Set.of(SMALLINT));
    } else if (currentVcl == SMALLINT || bestVcl == SMALLINT) {
      return calculateRelation(currentVcl, bestVcl, SMALLINT, Set.of(CARDINAL));
    } else if (currentVcl == CARDINAL || bestVcl == CARDINAL) {
      return calculateRelation(currentVcl, bestVcl, CARDINAL, Set.of(INTEGER));
    } else if (currentVcl == INTEGER || bestVcl == INTEGER) {
      return (bestVcl == INTEGER) ? -1 : 1;
    } else if (currentVcl == SINGLE || bestVcl == SINGLE) {
      return (bestVcl == SINGLE) ? -1 : 1;
    } else if (currentVcl == DOUBLE_CURRENCY || bestVcl == DOUBLE_CURRENCY) {
      return (bestVcl == DOUBLE_CURRENCY) ? -1 : 1;
    } else if (currentVcl == EXTENDED || bestVcl == EXTENDED) {
      return (bestVcl == EXTENDED) ? -1 : 1;
    } else if (currentVcl == WIDESTRING || bestVcl == WIDESTRING) {
      return (bestVcl == WIDESTRING) ? -1 : 1;
    } else if (currentVcl == UNICODESTRING || bestVcl == UNICODESTRING) {
      return (bestVcl == UNICODESTRING) ? -1 : 1;
    } else if (currentVcl == ANSISTRING || bestVcl == ANSISTRING) {
      return (bestVcl == ANSISTRING) ? -1 : 1;
    } else if (currentVcl == SHORTSTRING || bestVcl == SHORTSTRING) {
      return (bestVcl == SHORTSTRING) ? -1 : 1;
    } else if (currentVcl == ENUM || bestVcl == ENUM) {
      return (bestVcl == ENUM) ? -1 : 1;
    } else if (currentVcl == DYNAMIC_ARRAY || bestVcl == DYNAMIC_ARRAY) {
      return (bestVcl == DYNAMIC_ARRAY) ? -1 : 1;
    }

    // All possibilities should have been checked now.
    throw new AssertionError("Unhandled VariantConversionType!");
  }

  private static int calculateRelation(
      VariantConversionType currentVcl,
      VariantConversionType bestVcl,
      VariantConversionType testVcl,
      Set<VariantConversionType> conflictTypes) {
    if (conflictTypes.contains(bestVcl) || conflictTypes.contains(currentVcl)) {
      return 0;
    } else if (bestVcl == testVcl) {
      return -1;
    } else {
      return 1;
    }
  }
}
