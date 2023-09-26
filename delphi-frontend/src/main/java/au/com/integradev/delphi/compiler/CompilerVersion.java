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
package au.com.integradev.delphi.compiler;

import java.math.BigDecimal;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public final class CompilerVersion implements Comparable<CompilerVersion> {
  private static final Pattern VERSION_SYMBOL_PATTERN = Pattern.compile("VER\\d{2,3}");
  private static final Pattern VERSION_NUMBER_PATTERN = Pattern.compile("\\d{1,2}\\.\\d");
  private final String symbol;
  private final BigDecimal number;

  private CompilerVersion(String symbol, BigDecimal number) {
    this.symbol = symbol;
    this.number = number;
  }

  public static CompilerVersion fromVersionSymbol(String symbol) {
    if (!VERSION_SYMBOL_PATTERN.matcher(symbol).matches()) {
      throw new FormatException(
          "Version symbol \"" + symbol + "\" must match regex \"" + VERSION_SYMBOL_PATTERN + "\"");
    }

    return new CompilerVersion(
        symbol.toUpperCase(), new BigDecimal(symbol.substring(3)).movePointLeft(1));
  }

  public static CompilerVersion fromVersionNumber(String number) {
    if (!VERSION_NUMBER_PATTERN.matcher(number).matches()) {
      throw new FormatException(
          "Version number \"" + number + "\" must match regex \"" + VERSION_NUMBER_PATTERN + "\"");
    }

    return new CompilerVersion("VER" + StringUtils.remove(number, '.'), new BigDecimal(number));
  }

  public String symbol() {
    return symbol;
  }

  public BigDecimal number() {
    return number;
  }

  @Override
  public int compareTo(CompilerVersion compilerVersion) {
    return number.compareTo(compilerVersion.number);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CompilerVersion that = (CompilerVersion) o;
    return number.equals(that.number);
  }

  @Override
  public int hashCode() {
    return number.hashCode();
  }

  public static class FormatException extends RuntimeException {
    FormatException(String message) {
      super(message);
    }
  }
}
