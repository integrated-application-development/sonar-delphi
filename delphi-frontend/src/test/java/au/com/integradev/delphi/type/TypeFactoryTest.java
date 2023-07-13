/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package au.com.integradev.delphi.type;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.compiler.CompilerVersion;
import au.com.integradev.delphi.compiler.Toolchain;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.type.intrinsic.IntrinsicType;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class TypeFactoryTest {
  private static final String VERSION_3 = "VER100";
  private static final String VERSION_4 = "VER120";
  private static final String VERSION_2007 = "VER185";
  private static final String VERSION_2009 = "VER200";
  private static final String VERSION_XE7 = "VER280";
  private static final String VERSION_XE8 = "VER290";

  static class RealSizeArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(Arguments.of(VERSION_3, 6), Arguments.of(VERSION_4, 8));
    }
  }

  static class ExtendedSizeArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(Toolchain.DCCOSX, 16),
          Arguments.of(Toolchain.DCCIOS32, 16),
          Arguments.of(Toolchain.DCCLINUX64, 16),
          Arguments.of(Toolchain.DCC32, 10),
          Arguments.of(Toolchain.DCC64, 8),
          Arguments.of(Toolchain.DCCIOSARM, 8),
          Arguments.of(Toolchain.DCCIOSARM64, 8),
          Arguments.of(Toolchain.DCCAARM, 8),
          Arguments.of(Toolchain.DCCAARM64, 8));
    }
  }

  static class LongSizeArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(Toolchain.DCC32, VERSION_XE7, 4),
          Arguments.of(Toolchain.DCC64, VERSION_XE7, 4),
          Arguments.of(Toolchain.DCC32, VERSION_XE8, 4),
          Arguments.of(Toolchain.DCC64, VERSION_XE8, 4),
          Arguments.of(Toolchain.DCCIOSARM, VERSION_XE7, 4),
          Arguments.of(Toolchain.DCCIOSARM64, VERSION_XE7, 4),
          Arguments.of(Toolchain.DCCIOSARM, VERSION_XE8, 4),
          Arguments.of(Toolchain.DCCIOSARM64, VERSION_XE8, 8));
    }
  }

  static class NativeSizeArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(Toolchain.DCC32, VERSION_2007, 8),
          Arguments.of(Toolchain.DCC64, VERSION_2007, 8),
          Arguments.of(Toolchain.DCC32, VERSION_2009, 4),
          Arguments.of(Toolchain.DCC64, VERSION_2009, 8));
    }
  }

  static class PointerSizeArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(Arguments.of(Toolchain.DCC32, 4), Arguments.of(Toolchain.DCC64, 8));
    }
  }

  static class StringTypeArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(VERSION_2007, "System.AnsiString"),
          Arguments.of(VERSION_2009, "System.UnicodeString"));
    }
  }

  static class CharTypeArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(VERSION_2007, "System.AnsiChar"),
          Arguments.of(VERSION_2009, "System.WideChar"));
    }
  }

  @ParameterizedTest(name = "Real should be {1} bytes in {0}")
  @ArgumentsSource(RealSizeArgumentsProvider.class)
  void testSizeOfReal(String versionSymbol, int size) {
    TypeFactory typeFactory =
        new TypeFactoryImpl(Toolchain.DCC32, CompilerVersion.fromVersionSymbol(versionSymbol));
    assertThat(typeFactory.getIntrinsic(IntrinsicType.REAL).size()).isEqualTo(size);
  }

  @ParameterizedTest(name = "Extended should be {1} bytes on {0}")
  @ArgumentsSource(ExtendedSizeArgumentsProvider.class)
  void testSizeOfExtended(Toolchain toolchain, int size) {
    TypeFactory typeFactory =
        new TypeFactoryImpl(toolchain, DelphiProperties.COMPILER_VERSION_DEFAULT);
    assertThat(typeFactory.getIntrinsic(IntrinsicType.EXTENDED).size()).isEqualTo(size);
  }

  @ParameterizedTest(name = "LongInt and LongWord should be {2} bytes on {0} in {1}")
  @ArgumentsSource(LongSizeArgumentsProvider.class)
  void testSizeOfLongIntegers(Toolchain toolchain, String versionSymbol, int size) {
    TypeFactory typeFactory =
        new TypeFactoryImpl(toolchain, CompilerVersion.fromVersionSymbol(versionSymbol));
    assertThat(typeFactory.getIntrinsic(IntrinsicType.LONGINT).size()).isEqualTo(size);
    assertThat(typeFactory.getIntrinsic(IntrinsicType.LONGWORD).size()).isEqualTo(size);
  }

  @ParameterizedTest(name = "NativeInt and NativeUInt should be {2} bytes on {0} in {1}")
  @ArgumentsSource(NativeSizeArgumentsProvider.class)
  void testSizeOfNativeIntegers(Toolchain toolchain, String versionSymbol, int size) {
    TypeFactory typeFactory =
        new TypeFactoryImpl(toolchain, CompilerVersion.fromVersionSymbol(versionSymbol));
    assertThat(typeFactory.getIntrinsic(IntrinsicType.NATIVEINT).size()).isEqualTo(size);
    assertThat(typeFactory.getIntrinsic(IntrinsicType.NATIVEUINT).size()).isEqualTo(size);
  }

  @ParameterizedTest(name = "Pointers should be {1} bytes on {0}")
  @ArgumentsSource(PointerSizeArgumentsProvider.class)
  void testSizeOfPointers(Toolchain toolchain, int size) {
    TypeFactory typeFactory =
        new TypeFactoryImpl(toolchain, DelphiProperties.COMPILER_VERSION_DEFAULT);
    assertThat(typeFactory.getIntrinsic(IntrinsicType.POINTER).size()).isEqualTo(size);
  }

  @ParameterizedTest(name = "String should be \"{1}\" in {0}")
  @ArgumentsSource(StringTypeArgumentsProvider.class)
  void testTypeOfString(String versionSymbol, String signature) {
    TypeFactory typeFactory =
        new TypeFactoryImpl(Toolchain.DCC32, CompilerVersion.fromVersionSymbol(versionSymbol));
    assertThat(typeFactory.getIntrinsic(IntrinsicType.STRING).getImage()).isEqualTo(signature);
  }

  @ParameterizedTest(name = "Char should be \"{1}\" in {0}")
  @ArgumentsSource(CharTypeArgumentsProvider.class)
  void testTypeOfChar(String versionSymbol, String signature) {
    TypeFactory typeFactory =
        new TypeFactoryImpl(Toolchain.DCC32, CompilerVersion.fromVersionSymbol(versionSymbol));
    assertThat(typeFactory.getIntrinsic(IntrinsicType.CHAR).getImage()).isEqualTo(signature);
  }
}
