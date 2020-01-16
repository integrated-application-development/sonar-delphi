package org.sonar.plugins.delphi.symbol.resolve;

/**
 * Based directly off of the tequaltype enum from the FreePascal compiler.
 *
 * <p>The order of this enum is from lowest to highest priority. Note: The ordinal values of this
 * enum are compared with > and <.
 *
 * @see <a href="http://bit.ly/sym_const_tequaltype">tequaltype</a>
 */
enum EqualityType {
  INCOMPATIBLE_TYPES,
  CONVERT_OPERATOR,
  CONVERT_LEVEL_6,
  CONVERT_LEVEL_5,
  CONVERT_LEVEL_4,
  CONVERT_LEVEL_3,
  CONVERT_LEVEL_2,
  CONVERT_LEVEL_1,
  EQUAL,
  EXACT
}
