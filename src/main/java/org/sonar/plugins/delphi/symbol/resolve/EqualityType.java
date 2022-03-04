package org.sonar.plugins.delphi.symbol.resolve;

/**
 * Based directly off of the tequaltype enum from the FreePascal compiler.
 *
 * <p>The order of this enum is from lowest to highest priority. Note: The ordinal values of this
 * enum are compared with {@code >} and {@code <}.
 *
 * @see <a href="https://github.com/fpc/FPCSource/blob/main/compiler/symconst.pas#L817">tequaltype
 *     </a>
 */
enum EqualityType {
  INCOMPATIBLE_TYPES,
  CONVERT_LEVEL_8,
  CONVERT_LEVEL_7,
  CONVERT_LEVEL_6,
  CONVERT_LEVEL_5,
  CONVERT_LEVEL_4,
  CONVERT_LEVEL_3,
  CONVERT_LEVEL_2,
  CONVERT_LEVEL_1,
  EQUAL,
  EXACT
}
