/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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
package au.com.integradev.delphi.utils.format;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FormatStringParser {
  private final String input;
  private CharacterIterator iterator;

  public FormatStringParser(String input) {
    this.input = input;
  }

  private char current() {
    return iterator.current();
  }

  private char next() {
    return iterator.next();
  }

  private boolean done() {
    return iterator.current() == CharacterIterator.DONE;
  }

  public DelphiFormatString parse() {
    iterator = new StringCharacterIterator(input);
    List<FormatSpecifier> specifiers = new ArrayList<>();

    while (!done()) {
      if (current() == '%') {
        next(); // eat %
        if (current() != '%') {
          specifiers.add(parseFormatSpecifier());
        }
      }
      next();
    }

    return new DelphiFormatString(input, specifiers);
  }

  private FormatSpecifier parseFormatSpecifier() {
    Optional<Integer> index = Optional.empty();
    Optional<NumberOrWildcard> width;
    Optional<NumberOrWildcard> precision = Optional.empty();
    boolean leftJustified = false;
    Optional<NumberOrWildcard> indexOrWidth = parseIntLiteral();

    if (indexOrWidth.isPresent()) {
      if (current() == ':') {
        // Returns empty if it is a wildcard
        index = indexOrWidth.get().getValue();
        if (index.isEmpty()) {
          throw new DelphiFormatStringException("Wildcard width is not permitted");
        }

        next(); // eat :

        leftJustified = parseLeftJustifiedMarker();
        width = parseIntLiteral();
      } else {
        width = indexOrWidth;
      }
    } else {
      leftJustified = parseLeftJustifiedMarker();
      width = parseIntLiteral();
    }

    if (current() == '.') {
      next(); // eat .
      precision = parseIntLiteral();
    }

    Optional<FormatSpecifierType> type = parseFormatSpecifierType();
    if (type.isEmpty()) {
      throw new DelphiFormatStringException("Invalid format specifier type");
    }

    return new FormatSpecifier(
        type.get(), index.orElse(null), leftJustified, width.orElse(null), precision.orElse(null));
  }

  private boolean parseLeftJustifiedMarker() {
    if (current() == '-') {
      next(); // eat -
      return true;
    }

    return false;
  }

  private Optional<FormatSpecifierType> parseFormatSpecifierType() {
    char specifierChar = Character.toLowerCase(current());

    for (FormatSpecifierType type : FormatSpecifierType.values()) {
      if (type.getImage() == specifierChar) {
        return Optional.of(type);
      }
    }

    return Optional.empty();
  }

  private Optional<NumberOrWildcard> parseIntLiteral() {
    if (current() == '*') {
      next(); // eat *
      return Optional.of(NumberOrWildcard.wildcard());
    } else if (!Character.isDigit(current())) {
      return Optional.empty();
    }

    StringBuilder stringBuilder = new StringBuilder();

    while (Character.isDigit(current())) {
      stringBuilder.append(current());
      next();
    }

    return Optional.of(Integer.parseInt(stringBuilder.toString())).map(NumberOrWildcard::number);
  }
}
