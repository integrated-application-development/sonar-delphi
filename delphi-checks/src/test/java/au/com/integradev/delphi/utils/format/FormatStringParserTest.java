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

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class FormatStringParserTest {
  @Test
  void testParseSimpleFormatSpecifier() {
    List<FormatSpecifier> formatSpecifiers =
        new FormatStringParser("I am %s").parse().getFormatSpecifiers();

    assertThat(formatSpecifiers).hasSize(1);
    assertThat(formatSpecifiers.get(0).getType()).isEqualTo(FormatSpecifierType.STRING);
  }

  @Test
  void testParseIndexFormatSpecifier() {
    List<FormatSpecifier> formatSpecifiers =
        new FormatStringParser("I am %s - yes, %0:s").parse().getFormatSpecifiers();

    assertThat(formatSpecifiers).hasSize(2);
    assertThat(formatSpecifiers.get(0).getType()).isEqualTo(FormatSpecifierType.STRING);
    assertThat(formatSpecifiers.get(1).getType()).isEqualTo(FormatSpecifierType.STRING);

    Optional<Integer> index = formatSpecifiers.get(1).getIndex();

    assertThat(index).contains(0);
  }

  @Test
  void testParseMultipleFormatSpecifiers() {
    List<FormatSpecifier> formatSpecifiers =
        new FormatStringParser("I am %s and I am %d years old").parse().getFormatSpecifiers();

    assertThat(formatSpecifiers).hasSize(2);
    assertThat(formatSpecifiers.get(0).getType()).isEqualTo(FormatSpecifierType.STRING);
    assertThat(formatSpecifiers.get(1).getType()).isEqualTo(FormatSpecifierType.DECIMAL);
  }

  @ParameterizedTest(name = "[{index}] {0} requires {1} arguments")
  @CsvSource(value = {"%s %s %0:s,2", "%s %s,2", "%3:s,4", "%2:d %1:d %0:d %s,3"})
  void testCountIndexFormatSpecifiers(String formatString, int expectedCount) {
    DelphiFormatString fmt = new FormatStringParser(formatString).parse();
    assertThat(fmt.getArguments()).hasSize(expectedCount);
  }

  @ParameterizedTest
  @ValueSource(strings = {"%5f", "%5.2f", "%0:5.2f", "%0:-5.2f", "%-5f"})
  void testParseWidthSpecifier(String formatString) {
    List<FormatSpecifier> specifiers =
        new FormatStringParser(formatString).parse().getFormatSpecifiers();

    assertThat(specifiers).hasSize(1);
    assertThat(specifiers.get(0).getWidth()).contains(NumberOrWildcard.number(5));
  }

  @ParameterizedTest
  @ValueSource(strings = {"%*f", "%*.2f", "%0:*.2f", "%0:-*.2f", "%-*f"})
  void testParseWidthSpecifierWildcard(String formatString) {
    List<FormatSpecifier> specifiers =
        new FormatStringParser(formatString).parse().getFormatSpecifiers();

    assertThat(specifiers).hasSize(1);
    assertThat(specifiers.get(0).getWidth()).contains(NumberOrWildcard.wildcard());
  }

  @ParameterizedTest
  @ValueSource(strings = {"%.2f", "%5.2f", "%0:5.2f", "%0:-5.2f", "%-5.2f"})
  void testParsePrecisionSpecifier(String formatString) {
    List<FormatSpecifier> specifiers =
        new FormatStringParser(formatString).parse().getFormatSpecifiers();

    assertThat(specifiers).hasSize(1);
    assertThat(specifiers.get(0).getPrecision()).contains(NumberOrWildcard.number(2));
  }

  @ParameterizedTest
  @ValueSource(strings = {"%.*f", "%5.*f", "%0:5.*f", "%0:-5.*f", "%-5.*f"})
  void testParsePrecisionSpecifierWildcard(String formatString) {
    List<FormatSpecifier> specifiers =
        new FormatStringParser(formatString).parse().getFormatSpecifiers();

    assertThat(specifiers).hasSize(1);
    assertThat(specifiers.get(0).getPrecision()).contains(NumberOrWildcard.wildcard());
  }

  @Test
  void testParseIndexSpecifierWildcardRaisesException() {
    FormatStringParser parser = new FormatStringParser("%*:s");
    assertThatThrownBy(parser::parse).isInstanceOf(DelphiFormatStringException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {"%-.2f", "%-5.2f", "%0:-5.2f"})
  void testParseLeftJustified(String formatString) {
    List<FormatSpecifier> specifiers =
        new FormatStringParser(formatString).parse().getFormatSpecifiers();

    assertThat(specifiers).hasSize(1);
    assertThat(specifiers.get(0).isLeftJustified()).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"%.2f", "%5.2f", "%0:5.2f", "%0:5.2f-"})
  void testDefaultsToRightJustified(String formatString) {
    List<FormatSpecifier> specifiers =
        new FormatStringParser(formatString).parse().getFormatSpecifiers();

    assertThat(specifiers).hasSize(1);
    assertThat(specifiers.get(0).isLeftJustified()).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"%z", "%.af", "%0", "%0:", "%1.%d", "%*", "%*:d"})
  void testInvalidFormatStringRaisesException(String formatString) {
    FormatStringParser parser = new FormatStringParser(formatString);

    assertThatThrownBy(parser::parse).isInstanceOf(DelphiFormatStringException.class);
  }

  @ParameterizedTest(name = "[{index}] {0} parsed as {1}")
  @CsvSource(
      value = {
        "%d,DECIMAL",
        "%u,UNSIGNED_DECIMAL",
        "%e,SCIENTIFIC",
        "%f,FIXED",
        "%g,GENERAL",
        "%n,NUMBER",
        "%m,MONEY",
        "%p,POINTER",
        "%s,STRING",
        "%x,HEXADECIMAL",
        "%D,DECIMAL",
        "%U,UNSIGNED_DECIMAL",
        "%E,SCIENTIFIC",
        "%F,FIXED",
        "%G,GENERAL",
        "%N,NUMBER",
        "%M,MONEY",
        "%P,POINTER",
        "%S,STRING",
        "%X,HEXADECIMAL"
      })
  void testParseFormatSpecifierType(String formatString, FormatSpecifierType expectedType) {
    List<FormatSpecifier> specifiers =
        new FormatStringParser(formatString).parse().getFormatSpecifiers();

    assertThat(specifiers).hasSize(1);
    assertThat(specifiers.get(0).getType()).isEqualTo(expectedType);
  }

  @Test
  void testParseStringWithNoSpecifiers() {
    assertThat(new FormatStringParser("Hello world").parse().getFormatSpecifiers()).isEmpty();
  }

  @Test
  void testParseStringWithEscapedPercents() {
    assertThat(new FormatStringParser("Hello %% world").parse().getFormatSpecifiers()).isEmpty();
  }

  @Test
  void testGetFormatSpecifierTypesForArguments() {
    List<DelphiFormatArgument> arguments =
        new FormatStringParser("%s %d %*.*f %0:e %*.f").parse().getArguments();

    Assertions.assertThat(arguments).hasSize(5);
    assertThat(arguments.get(0).getTypes())
        .containsExactlyInAnyOrder(FormatSpecifierType.STRING, FormatSpecifierType.SCIENTIFIC);
    assertThat(arguments.get(1).getTypes()).containsExactlyInAnyOrder(FormatSpecifierType.DECIMAL);
    assertThat(arguments.get(2).getTypes())
        .containsExactlyInAnyOrder(FormatSpecifierType.DECIMAL, FormatSpecifierType.FIXED);
    assertThat(arguments.get(3).getTypes())
        .containsExactlyInAnyOrder(FormatSpecifierType.UNSIGNED_DECIMAL);
    assertThat(arguments.get(4).getTypes()).containsExactlyInAnyOrder(FormatSpecifierType.FIXED);
  }

  @Test
  void testGetFormatSpecifiersForArguments() {
    List<DelphiFormatArgument> arguments =
        new FormatStringParser("%s %d %*.*f %0:e %*.f").parse().getArguments();

    Assertions.assertThat(arguments).hasSize(5);
    assertThat(arguments.get(0).getSpecifiers()).hasSize(2); // %s and %e
    assertThat(arguments.get(1).getSpecifiers()).hasSize(1); // %d
    assertThat(arguments.get(2).getSpecifiers()).hasSize(1); // %f
    assertThat(arguments.get(3).getSpecifiers()).isEmpty();
    assertThat(arguments.get(4).getSpecifiers()).hasSize(1); // %f
  }
}
