package org.sonar.plugins.delphi.compiler;

import java.math.BigDecimal;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public final class CompilerVersion implements Comparable<CompilerVersion> {
  private static final String VERSION_SYMBOL_REGEX = "VER[0-9]{2,3}";
  private static final String VERSION_NUMBER_REGEX = "[0-9]{1,2}\\.[0-9]";
  private final String symbol;
  private final BigDecimal number;

  private CompilerVersion(String symbol, BigDecimal number) {
    this.symbol = symbol;
    this.number = number;
  }

  public static CompilerVersion fromVersionSymbol(String symbol) {
    if (!symbol.matches(VERSION_SYMBOL_REGEX)) {
      throw new FormatException(
          "Version symbol \"" + symbol + "\" must match regex \"" + VERSION_SYMBOL_REGEX + "\"");
    }

    return new CompilerVersion(
        symbol.toUpperCase(), new BigDecimal(symbol.substring(3)).movePointLeft(1));
  }

  public static CompilerVersion fromVersionNumber(String number) {
    if (!number.matches(VERSION_NUMBER_REGEX)) {
      throw new FormatException(
          "Version number \"" + number + "\" must match regex \"" + VERSION_NUMBER_REGEX + "\"");
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
  public int compareTo(@NotNull CompilerVersion compilerVersion) {
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
