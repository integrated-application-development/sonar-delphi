package org.sonar.plugins.delphi.symbol.resolve;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Math.nextAfter;
import static java.util.function.Predicate.not;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_1;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_2;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_3;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_6;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.EQUAL;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.EXACT;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.INCOMPATIBLE_TYPES;
import static org.sonar.plugins.delphi.symbol.resolve.VariantEqualityType.ANSISTRING;
import static org.sonar.plugins.delphi.symbol.resolve.VariantEqualityType.BYTE;
import static org.sonar.plugins.delphi.symbol.resolve.VariantEqualityType.CARDINAL;
import static org.sonar.plugins.delphi.symbol.resolve.VariantEqualityType.DOUBLE_CURRENCY;
import static org.sonar.plugins.delphi.symbol.resolve.VariantEqualityType.EXTENDED;
import static org.sonar.plugins.delphi.symbol.resolve.VariantEqualityType.FORMAL_BOOLEAN;
import static org.sonar.plugins.delphi.symbol.resolve.VariantEqualityType.INCOMPATIBLE_VARIANT;
import static org.sonar.plugins.delphi.symbol.resolve.VariantEqualityType.LONGINT;
import static org.sonar.plugins.delphi.symbol.resolve.VariantEqualityType.SHORTINT;
import static org.sonar.plugins.delphi.symbol.resolve.VariantEqualityType.SINGLE;
import static org.sonar.plugins.delphi.symbol.resolve.VariantEqualityType.SMALLINT;
import static org.sonar.plugins.delphi.symbol.resolve.VariantEqualityType.UNICODESTRING;
import static org.sonar.plugins.delphi.symbol.resolve.VariantEqualityType.WIDESTRING;
import static org.sonar.plugins.delphi.symbol.resolve.VariantEqualityType.WORD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.plugins.delphi.symbol.ParameterDeclaration;
import org.sonar.plugins.delphi.type.DelphiIntrinsicType.DecimalType;
import org.sonar.plugins.delphi.type.DelphiIntrinsicType.IntegerType;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.CollectionType;
import org.sonar.plugins.delphi.type.Type.FileType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;

/**
 * Resolves an invocation to the correct declaration. Based directly off of the tcallcandidates
 * class from the FreePascal compiler
 *
 * @see <a href="https://github.com/graemeg/freepascal/blob/master/compiler/htypechk.pas#L69">
 *     tcallcandidates</a>
 */
public class InvocationResolver {
  private List<InvocationCandidate> candidates = new ArrayList<>();

  private List<InvocationArgument> arguments = new ArrayList<>();

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
        ParameterDeclaration parameter = candidate.getData().getParameter(i);
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
      InvocationCandidate candidate, InvocationArgument argument, ParameterDeclaration parameter) {
    Type argumentType = argument.getType();
    Type parameterType = parameter.getType();
    boolean ambiguousMethodReference = false;

    // Convert ProceduralType to its returnType when not expecting a procedural type
    if (argumentType.isProcedural() && !parameterType.isProcedural()) {
      argumentType = ((ProceduralType) argumentType).returnType();
      ambiguousMethodReference = argument.looksLikeMethodReference();
    }

    // If the parameter expects a procedural type then we need to find the overload that the
    // argument is referring to.
    if (argument.isMethodReference(parameterType)) {
      argumentType = Objects.requireNonNull(argument.findMethodReferenceType(parameterType));
    }

    EqualityType equality;

    if (Objects.requireNonNull(argumentType).is(Objects.requireNonNull(parameterType))) {
      equality = EXACT;
    } else if (!equalTypeRequired(parameter)
        && isRealOrExtended(argumentType)
        && isRealOrExtended(parameterType)) {
      equality = EQUAL;
      int rth;
      int rfh;

      if (parameterType.is(DecimalType.EXTENDED.type)) {
        rth = 4;
      } else if (isReal(parameterType)) {
        rth = 2;
      } else {
        rth = 1;
      }

      if (argumentType.is(DecimalType.EXTENDED.type)) {
        rfh = 4;
      } else if (isReal(argumentType)) {
        rfh = 2;
      } else {
        rfh = 1;
      }

      // Penalty for shrinking of precision
      if (rth < rfh) {
        rfh = (rfh - rth) * 16;
      } else {
        rfh = rth - rfh;
      }

      candidate.increaseOrdinalDistance(rfh);
    } else if (!equalTypeRequired(parameter)
        && argumentType.isObject()
        && parameterType.isObject()
        && argumentType.isSubTypeOf(parameterType)) {
      equality = CONVERT_LEVEL_1;
      Type comparisonType = argumentType;
      while ((comparisonType = comparisonType.superType()) != DelphiType.unknownType()) {
        candidate.increaseOrdinalDistance(1);
        if (comparisonType.is(parameterType)) {
          break;
        }
      }
    } else {
      // Generic type comparison
      equality = TypeComparer.compare(argumentType, parameterType);

      if (equality.ordinal() < EQUAL.ordinal() && equalTypeRequired(parameter)) {
        // Parameter requires an equal type so the previous match was not good enough
        equality = varParameterAllowed(argumentType, parameter);
      }
    }

    if (!equalTypeRequired(parameter) && argumentType.isInteger() && parameterType.isInteger()) {
      var argData = IntegerType.fromType(argumentType);
      var paramData = IntegerType.fromType(parameterType);

      candidate.increaseOrdinalDistance(argData.ordinalDistance(paramData));

      if (argData.isSigned() != paramData.isSigned()) {
        candidate.setOrdinalDistance(nextAfter(candidate.getOrdinalDistance(), POSITIVE_INFINITY));
      }
    }

    // When an ambiguous procedural type was changed to an invocation, an exact match is
    // downgraded to equal.
    // Ordinal distance is also bumped.
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
      case CONVERT_OPERATOR:
        candidate.incrementConvertOperatorCount();
        break;
      case INCOMPATIBLE_TYPES:
        candidate.setInvalid();
        break;
      default:
        // Do nothing
    }
  }

  private EqualityType varParameterAllowed(Type argType, ParameterDeclaration parameter) {
    Type paramType = parameter.getType();

    if (paramType.isUntyped() && !parameter.isConst()) {
      return CONVERT_LEVEL_6;
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
        && !((FileType) paramType).fileType().isUntyped()
        && ((FileType) argType).fileType().isUntyped()) {
      // An implicit file conversion is allowed from a typed file to an untyped one
      return CONVERT_LEVEL_1;
    }

    return INCOMPATIBLE_TYPES;
  }

  private static boolean equalTypeRequired(ParameterDeclaration parameter) {
    return parameter.isOut() || parameter.isVar();
  }

  private static boolean isRealOrExtended(Type type) {
    return isReal(type) || DecimalType.EXTENDED.type.is(type);
  }

  private static boolean isReal(Type type) {
    return DecimalType.DOUBLE.type.is(type) || DecimalType.REAL.type.is(type);
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
    boolean singleVariant = arguments.size() == 1 && arguments.get(0).getType().isVariant();

    for (int i = 0; i < candidates.size(); ++i) {
      InvocationCandidate candidate = candidates.get(i);
      int result =
          singleVariant
              ? isBetterCandidateSingleVariant(candidate, bestCandidate)
              : isBetterCandidate(candidate, bestCandidate);

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
    int result = 0;

    if (bestCandidate.isInvalid() && !candidate.isInvalid()) {
      result = 1;
    }

    if (candidate.isInvalid() && !bestCandidate.isInvalid()) {
      result = -1;
    }

    if (result == 0) {
      // Less operator parameters?
      result = bestCandidate.getConvertOperatorCount() - candidate.getConvertOperatorCount();
    }

    for (int i = 1; i <= InvocationCandidate.CONVERT_LEVELS; ++i) {
      if (result == 0) {
        // Less castLevel[6..1] parameters?
        result = bestCandidate.getConvertLevelCount(i) - candidate.getConvertLevelCount(i);
      }
    }

    if (result == 0) {
      // More exact parameters?
      result = candidate.getExactCount() - bestCandidate.getExactCount();
    }

    if (result == 0) {
      // Less equal parameters?
      result = bestCandidate.getEqualCount() - candidate.getEqualCount();
    }

    if (result == 0) {
      // Smaller ordinal distance?
      if (candidate.getOrdinalDistance() < bestCandidate.getOrdinalDistance()) {
        result = 1;
      } else if (candidate.getOrdinalDistance() > bestCandidate.getOrdinalDistance()) {
        result = -1;
      }
    }

    return result;
  }

  /**
   * Delphi precedence rules extracted from test programs. Only valid if passing a variant argument
   * to overloaded procedures expecting exactly one parameter.
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
   * @param candidate Candidate we're checking
   * @param bestCandidate The current best candidate
   * @return
   *     <ul>
   *       <li>> 0 when candidate is better than bestCandidate
   *       <li>< 0 when bestCandidate is better than candidate
   *       <li>= 0 when both are equal
   *     </ul>
   *
   * @see <a href="https://github.com/graemeg/freepascal/blob/master/compiler/htypechk.pas#L3339">
   *     is_better_candidate_single_variant</a>
   */
  private int isBetterCandidateSingleVariant(
      InvocationCandidate candidate, InvocationCandidate bestCandidate) {
    Type current = candidate.getData().getParameter(0).getType();
    Type best = bestCandidate.getData().getParameter(0).getType();

    // If one of the parameters is a regular variant, fall back to the default algorithm
    if (current.isVariant() || best.isVariant()) {
      return isBetterCandidate(candidate, bestCandidate);
    }

    VariantEqualityType currentVcl = VariantEqualityType.fromType(current);
    VariantEqualityType bestVcl = VariantEqualityType.fromType(best);

    if (currentVcl == INCOMPATIBLE_VARIANT) {
      return -1;
    } else if (bestVcl == INCOMPATIBLE_VARIANT) {
      return 1;
    } else if (currentVcl == bestVcl) {
      return 0;
    } else if (currentVcl == FORMAL_BOOLEAN || bestVcl == FORMAL_BOOLEAN) {
      if (currentVcl == FORMAL_BOOLEAN) {
        return VariantEqualityType.isChari64Str(bestVcl) ? 1 : 0;
      } else {
        return VariantEqualityType.isChari64Str(currentVcl) ? -1 : 0;
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
      return calculateRelation(currentVcl, bestVcl, CARDINAL, Set.of(LONGINT));
    } else if (currentVcl == LONGINT || bestVcl == LONGINT) {
      return (bestVcl == LONGINT) ? -1 : 1;
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
    }

    // All possibilities should have been checked now.
    throw new AssertionError("Unhandled VariantEqualityType!");
  }

  private int calculateRelation(
      VariantEqualityType currentVcl,
      VariantEqualityType bestVcl,
      VariantEqualityType testVcl,
      Set<VariantEqualityType> conflictTypes) {
    if (conflictTypes.contains(bestVcl) || conflictTypes.contains(currentVcl)) {
      return 0;
    } else if (bestVcl == testVcl) {
      return -1;
    } else {
      return 1;
    }
  }
}
