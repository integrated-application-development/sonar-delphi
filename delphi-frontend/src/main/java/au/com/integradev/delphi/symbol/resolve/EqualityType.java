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

/**
 * Based directly off of the tequaltype enum from the FreePascal compiler.
 *
 * <p>The order of this enum is from lowest to highest priority. Note: The ordinal values of this
 * enum are compared with {@code >} and {@code <}.
 *
 * @see <a href="https://github.com/fpc/FPCSource/blob/main/compiler/symconst.pas#L817">tequaltype
 *     </a>
 */
public enum EqualityType {
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
