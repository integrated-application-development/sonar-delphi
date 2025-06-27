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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.ANSICHAR;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.ANSISTRING;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.BOOLEAN;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.BYTE;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.BYTEBOOL;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.CARDINAL;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.CHAR;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.COMP;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.CURRENCY;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.DOUBLE;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.EXTENDED;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.INT64;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.INTEGER;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.LONGBOOL;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.LONGINT;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.LONGWORD;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.NATIVEINT;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.NATIVEUINT;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.PANSICHAR;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.PWIDECHAR;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.REAL;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.REAL48;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.SHORTINT;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.SHORTSTRING;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.SINGLE;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.SMALLINT;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.UINT64;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.UNICODESTRING;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.VARIANT;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.WIDECHAR;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.WIDESTRING;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.WORD;
import static org.sonar.plugins.communitydelphi.api.type.IntrinsicType.WORDBOOL;
import static org.sonar.plugins.communitydelphi.api.type.StructKind.CLASS;
import static org.sonar.plugins.communitydelphi.api.type.StructKind.INTERFACE;
import static org.sonar.plugins.communitydelphi.api.type.StructKind.RECORD;
import static org.sonar.plugins.communitydelphi.api.type.TypeFactory.untypedType;

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.compiler.Toolchain;
import au.com.integradev.delphi.type.factory.ArrayOption;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.type.parameter.FormalParameter;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import au.com.integradev.delphi.utils.types.TypeMocker;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.symbol.Invocable;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.CodePages;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class InvocationResolverTest {
  private TypeFactory factory;
  private Set<InvocationCandidate> resolved;

  @BeforeEach
  void setup() {
    factory = TypeFactoryUtils.defaultFactory();
  }

  private void assertResolved(Type argumentType, Type winnerType, Type loserType) {
    assertResolved(List.of(argumentType), List.of(winnerType), List.of(loserType));
  }

  private Type type(IntrinsicType intrinsic) {
    return factory.getIntrinsic(intrinsic);
  }

  private Type subrange(String image, int min, int max) {
    return factory.subrange(image, BigInteger.valueOf(min), BigInteger.valueOf(max));
  }

  private Type subrange(String image, Type type) {
    return factory.subrange(image, type);
  }

  private void assertResolved(
      List<Type> argumentTypes, List<Type> winnerTypes, List<Type> loserTypes) {
    InvocationCandidate winner = mockCandidate(winnerTypes, false);
    InvocationCandidate loser = mockCandidate(loserTypes, false);

    runResolver(argumentTypes, List.of(winner, loser));
    assertThat(resolved).containsOnly(winner);
  }

  private void assertResolvedVar(Type argumentType, Type winnerType) {
    runResolver(
        List.of(argumentType), Collections.singletonList(mockCandidate(List.of(winnerType), true)));
  }

  private void assertAmbiguous(Type argumentType, Type... candidates) {
    runResolver(
        List.of(argumentType),
        Arrays.stream(candidates)
            .map(type -> mockCandidate(List.of(type), false))
            .collect(Collectors.toList()));

    assertThat(resolved).hasSizeGreaterThan(1);
  }

  private void assertIncompatible(Type argumentType, Type... candidates) {
    runResolver(
        List.of(argumentType),
        Arrays.stream(candidates)
            .map(type -> mockCandidate(List.of(type), false))
            .collect(Collectors.toList()));

    assertThat(resolved).isEmpty();
  }

  private void runResolver(List<Type> argumentTypes, List<InvocationCandidate> candidates) {
    InvocationResolver resolver = new InvocationResolver();

    for (Type argumentType : argumentTypes) {
      InvocationArgument argument = mock(InvocationArgument.class);
      when(argument.getType()).thenReturn(argumentType);
      resolver.addArgument(argument);
    }

    candidates.forEach(resolver::addCandidate);
    resolver.processCandidates();

    resolved = resolver.chooseBest();
  }

  private static InvocationCandidate mockCandidate(List<Type> parameterTypes, boolean isVar) {
    Invocable invocable = mock(Invocable.class);
    List<Parameter> parameters = new ArrayList<>();

    for (Type paramType : parameterTypes) {
      FormalParameterData formalParameterData = mock(FormalParameterData.class);
      when(formalParameterData.getType()).thenReturn(paramType);
      if (isVar) {
        when(formalParameterData.isVar()).thenReturn(true);
      }
      parameters.add(FormalParameter.create(formalParameterData));
    }

    when(invocable.getParameters()).thenReturn(parameters);
    when(invocable.getParameter(anyInt())).thenCallRealMethod();
    when(invocable.getParametersCount()).thenCallRealMethod();
    when(invocable.getRequiredParametersCount()).thenCallRealMethod();

    return new InvocationCandidate(invocable);
  }

  @Test
  void testIntegerTypes() {
    assertResolved(type(INTEGER), type(LONGINT), type(BYTE));
    assertResolved(type(INTEGER), type(LONGINT), type(INT64));
    assertResolved(type(INTEGER), type(INT64), type(UINT64));
    assertResolved(type(INTEGER), type(WORD), type(SHORTINT));
    assertResolved(type(INTEGER), type(WORD), type(BYTE));
    assertResolved(type(SHORTINT), type(NATIVEINT), type(NATIVEUINT));

    Type hwnd = factory.strongAlias("HWND", type(NATIVEUINT));
    assertResolved(
        List.of(hwnd, type(NATIVEUINT), type(SHORTINT), type(SHORTINT)),
        List.of(hwnd, type(NATIVEUINT), type(NATIVEINT), type(NATIVEINT)),
        List.of(hwnd, type(NATIVEUINT), type(NATIVEUINT), type(NATIVEINT)));

    assertResolved(type(BYTE), type(INTEGER), type(DOUBLE));

    assertResolved(factory.strongAlias("MyWord", type(LONGWORD)), type(INT64), type(INTEGER));
    assertResolved(type(LONGWORD), factory.strongAlias("MyInt64", type(INT64)), type(INTEGER));
  }

  @ParameterizedTest
  @EnumSource(
      value = Toolchain.class,
      names = {"DCC32", "DCC64"})
  void testRealTypes(Toolchain toolchain) {
    factory = new TypeFactoryImpl(toolchain, DelphiProperties.COMPILER_VERSION_DEFAULT);
    assertResolved(type(EXTENDED), type(DOUBLE), type(SINGLE));
    assertResolved(type(EXTENDED), type(REAL), type(SINGLE));
    assertResolved(type(DOUBLE), type(EXTENDED), type(SINGLE));
    assertResolved(type(REAL), type(EXTENDED), type(SINGLE));
  }

  @ParameterizedTest
  @EnumSource(
      value = Toolchain.class,
      names = {"DCC32", "DCC64"})
  void testMixedToRealTypes(Toolchain toolchain) {
    factory = new TypeFactoryImpl(toolchain, DelphiProperties.COMPILER_VERSION_DEFAULT);
    assertResolved(
        List.of(type(EXTENDED), type(INTEGER)),
        List.of(type(EXTENDED), type(EXTENDED)),
        List.of(type(SINGLE), type(SINGLE)));
    assertResolved(
        List.of(type(EXTENDED), type(INTEGER)),
        List.of(type(EXTENDED), type(EXTENDED)),
        List.of(type(DOUBLE), type(DOUBLE)));
    assertResolved(
        List.of(type(INTEGER), type(INTEGER)),
        List.of(type(SINGLE), type(SINGLE)),
        List.of(type(EXTENDED), type(EXTENDED)));
  }

  @ParameterizedTest
  @EnumSource(
      value = Toolchain.class,
      names = {"DCC32", "DCC64"})
  void testIntegerToRealTypes(Toolchain toolchain) {
    factory = new TypeFactoryImpl(toolchain, DelphiProperties.COMPILER_VERSION_DEFAULT);
    assertResolved(
        List.of(type(SHORTINT), type(SHORTINT)),
        List.of(type(INTEGER), type(INTEGER)),
        List.of(type(SINGLE), type(SINGLE)));
    assertResolved(
        List.of(type(SHORTINT), type(SHORTINT)),
        List.of(type(INTEGER), type(INTEGER)),
        List.of(type(DOUBLE), type(DOUBLE)));
    assertResolved(
        List.of(type(SHORTINT), type(SHORTINT)),
        List.of(type(INTEGER), type(INTEGER)),
        List.of(type(EXTENDED), type(EXTENDED)));
  }

  @Test
  void testTextTypes() {
    assertResolved(
        factory.strongAlias("MyString", type(UNICODESTRING)),
        type(UNICODESTRING),
        type(SHORTSTRING));
    assertResolved(
        type(UNICODESTRING),
        factory.strongAlias("MyString", type(UNICODESTRING)),
        type(SHORTSTRING));
    assertResolved(
        List.of(type(ANSICHAR), type(ANSICHAR)),
        List.of(type(ANSISTRING), type(ANSISTRING)),
        List.of(type(UNICODESTRING), type(UNICODESTRING)));
    assertResolved(
        List.of(type(ANSISTRING), type(ANSISTRING)),
        List.of(type(ANSISTRING), type(ANSISTRING)),
        List.of(type(UNICODESTRING), type(UNICODESTRING)));
    assertResolved(
        List.of(type(ANSICHAR), type(SHORTSTRING)),
        List.of(type(ANSISTRING), type(ANSISTRING)),
        List.of(type(UNICODESTRING), type(UNICODESTRING)));
    assertResolved(
        List.of(type(ANSISTRING), type(SHORTSTRING)),
        List.of(type(ANSISTRING), type(ANSISTRING)),
        List.of(type(UNICODESTRING), type(UNICODESTRING)));
    assertResolved(
        List.of(type(SHORTSTRING), type(SHORTSTRING)),
        List.of(type(ANSISTRING), type(ANSISTRING)),
        List.of(type(UNICODESTRING), type(UNICODESTRING)));
    assertResolved(
        List.of(type(ANSICHAR), type(CHAR)),
        List.of(type(UNICODESTRING), type(UNICODESTRING)),
        List.of(type(ANSISTRING), type(ANSISTRING)));
    assertResolved(
        List.of(type(ANSISTRING), type(UNICODESTRING)),
        List.of(type(UNICODESTRING), type(UNICODESTRING)),
        List.of(type(ANSISTRING), type(ANSISTRING)));
    assertResolved(
        List.of(type(SHORTSTRING), type(CHAR)),
        List.of(type(UNICODESTRING), type(UNICODESTRING)),
        List.of(type(ANSISTRING), type(ANSISTRING)));
    assertResolved(
        List.of(type(SHORTSTRING), type(UNICODESTRING)),
        List.of(type(UNICODESTRING), type(UNICODESTRING)),
        List.of(type(ANSISTRING), type(ANSISTRING)));
    assertResolved(
        List.of(type(CHAR), type(CHAR)),
        List.of(type(UNICODESTRING), type(UNICODESTRING)),
        List.of(type(ANSISTRING), type(ANSISTRING)));

    assertResolved(type(UNICODESTRING), type(VARIANT), type(ANSISTRING));
    assertResolved(type(UNICODESTRING), type(VARIANT), type(SHORTSTRING));
    assertResolved(type(WIDESTRING), type(VARIANT), type(ANSISTRING));
    assertResolved(type(WIDESTRING), type(VARIANT), type(SHORTSTRING));
    assertResolved(type(ANSISTRING), type(VARIANT), type(SHORTSTRING));
    assertResolved(type(SHORTSTRING), type(ANSISTRING), type(VARIANT));

    assertResolved(type(PANSICHAR), type(ANSISTRING), factory.ansiString(CodePages.CP_1252));
    assertResolved(type(PANSICHAR), factory.ansiString(CodePages.CP_1252), type(UNICODESTRING));
    assertResolved(type(PANSICHAR), type(UNICODESTRING), type(WIDESTRING));
    assertResolved(type(PANSICHAR), type(WIDESTRING), type(SHORTSTRING));

    assertResolved(type(PWIDECHAR), type(UNICODESTRING), type(WIDESTRING));
    assertResolved(type(PWIDECHAR), type(WIDESTRING), type(ANSISTRING));
    assertResolved(type(PWIDECHAR), type(ANSISTRING), type(SHORTSTRING));
    assertIncompatible(type(PWIDECHAR), type(SHORTSTRING));
  }

  @ParameterizedTest
  @EnumSource(
      value = Toolchain.class,
      names = {"DCC32", "DCC64"})
  void testVariantTypes(Toolchain toolchain) {
    factory = new TypeFactoryImpl(toolchain, DelphiProperties.COMPILER_VERSION_DEFAULT);

    Type incompatibleType = TypeMocker.struct("Incompatible", RECORD);
    Type enumeration = ((TypeFactoryImpl) factory).enumeration("E", DelphiScope.unknownScope());
    Type dynamicArray =
        ((TypeFactoryImpl) factory).array(null, type(INTEGER), Set.of(ArrayOption.DYNAMIC));

    assertResolved(
        List.of(type(UNICODESTRING), incompatibleType, type(BOOLEAN)),
        List.of(type(UNICODESTRING), incompatibleType, type(VARIANT), type(BOOLEAN)),
        List.of(type(UNICODESTRING), type(VARIANT), type(BOOLEAN)));
    assertResolved(type(UNICODESTRING), type(VARIANT), type(ANSISTRING));
    assertResolved(type(UNICODESTRING), type(VARIANT), type(SHORTSTRING));
    assertResolved(type(SHORTSTRING), type(ANSISTRING), type(VARIANT));
    assertResolved(type(ANSISTRING), type(VARIANT), type(SHORTSTRING));
    assertResolved(type(ANSISTRING), type(UNICODESTRING), type(VARIANT));
    assertResolved(type(ANSISTRING), factory.ansiString(CodePages.CP_UTF8), type(VARIANT));
    assertResolved(type(ANSISTRING), factory.ansiString(CodePages.CP_1252), type(VARIANT));
    assertResolved(factory.ansiString(1251), factory.ansiString(1252), type(VARIANT));

    assertResolved(
        List.of(type(VARIANT), type(BYTE)),
        List.of(type(UNICODESTRING), type(INTEGER)),
        List.of(type(ANSISTRING), type(INTEGER)));

    Type iinterface = TypeMocker.struct("System.IInterface", INTERFACE);
    assertResolved(type(VARIANT), iinterface, TypeFactory.untypedType());
    assertIncompatible(type(VARIANT), TypeMocker.struct("System.NotIInterface", INTERFACE));

    assertResolved(type(VARIANT), TypeFactory.untypedType(), type(EXTENDED));
    assertResolved(type(VARIANT), TypeFactory.untypedType(), type(CURRENCY));
    assertResolved(type(VARIANT), TypeFactory.untypedType(), type(COMP));
    assertResolved(type(VARIANT), TypeFactory.untypedType(), type(UINT64));
    assertResolved(type(VARIANT), TypeFactory.untypedType(), type(UNICODESTRING));
    assertResolved(type(VARIANT), TypeFactory.untypedType(), type(BOOLEAN));

    assertResolved(type(VARIANT), type(EXTENDED), type(CURRENCY));
    assertResolved(type(VARIANT), type(CURRENCY), type(UINT64));
    assertResolved(type(VARIANT), type(EXTENDED), type(COMP));
    assertResolved(type(VARIANT), type(COMP), type(UINT64));

    assertAmbiguous(type(VARIANT), type(CURRENCY), type(DOUBLE));
    assertAmbiguous(type(VARIANT), type(COMP), type(DOUBLE));
    assertAmbiguous(type(VARIANT), type(CURRENCY), type(COMP));

    assertResolved(type(VARIANT), type(EXTENDED), type(DOUBLE));
    assertResolved(type(VARIANT), type(DOUBLE), type(REAL48));
    assertResolved(type(VARIANT), type(REAL48), type(SINGLE));
    assertResolved(type(VARIANT), type(SINGLE), incompatibleType);

    assertResolved(type(VARIANT), type(UINT64), type(INT64));
    assertResolved(type(VARIANT), type(INT64), type(CARDINAL));
    assertResolved(type(VARIANT), type(CARDINAL), type(INTEGER));
    assertResolved(type(VARIANT), type(INTEGER), type(WORD));
    assertResolved(type(VARIANT), type(SMALLINT), type(BYTE));
    assertResolved(type(VARIANT), type(SHORTINT), subrange("ShorterInt", 1, 127));

    assertResolved(type(VARIANT), subrange("A", 1, 10), subrange("B", 1, 9));
    assertResolved(type(VARIANT), subrange("A", -2, 10), subrange("B", -1, 9));
    assertResolved(type(VARIANT), subrange("A", 0, 10), subrange("B", -300, 9));
    assertResolved(type(VARIANT), subrange("A", -300, 10), subrange("B", 0, 10));
    assertAmbiguous(type(VARIANT), subrange("A", 0, 10), subrange("B", 1, 100000));

    assertResolved(type(VARIANT), type(WIDESTRING), type(UNICODESTRING));
    assertResolved(type(VARIANT), type(UNICODESTRING), type(ANSISTRING));
    assertResolved(type(VARIANT), type(ANSISTRING), type(SHORTSTRING));
    assertResolved(type(VARIANT), type(SHORTSTRING), incompatibleType);

    assertResolved(type(VARIANT), type(BOOLEAN), incompatibleType);
    assertAmbiguous(type(VARIANT), type(BOOLEAN), type(BYTEBOOL));
    assertAmbiguous(type(VARIANT), type(BOOLEAN), type(WORDBOOL));
    assertAmbiguous(type(VARIANT), type(BOOLEAN), type(LONGBOOL));
    assertAmbiguous(type(VARIANT), type(BOOLEAN), type(INTEGER));

    assertResolved(type(VARIANT), enumeration, incompatibleType);
    assertAmbiguous(type(VARIANT), enumeration, subrange("S", enumeration));
    assertAmbiguous(type(VARIANT), enumeration, type(EXTENDED));
    assertAmbiguous(type(VARIANT), enumeration, type(UINT64));
    assertAmbiguous(type(VARIANT), enumeration, type(WIDESTRING));

    assertResolved(type(VARIANT), dynamicArray, incompatibleType);
    assertAmbiguous(type(VARIANT), dynamicArray, type(EXTENDED));
    assertAmbiguous(type(VARIANT), dynamicArray, type(UINT64));
    assertAmbiguous(type(VARIANT), dynamicArray, type(WIDESTRING));

    assertIncompatible(type(VARIANT), type(WIDECHAR));
    assertIncompatible(type(VARIANT), type(ANSICHAR));
  }

  @Test
  void testInheritedTypes() {
    Type grandparent = TypeMocker.struct("Grandparent", CLASS);
    Type parent = TypeMocker.struct("Parent", CLASS, grandparent);
    Type child = TypeMocker.struct("Child", CLASS, parent);

    assertResolved(child, parent, grandparent);
    assertResolved(parent, grandparent, child);
  }

  @Test
  void testVarParameters() {
    Type openArray =
        ((TypeFactoryImpl) factory).array(null, type(INTEGER), Set.of(ArrayOption.OPEN));
    Type dynamicArray =
        ((TypeFactoryImpl) factory).array(null, type(INTEGER), Set.of(ArrayOption.DYNAMIC));

    assertResolvedVar(type(INTEGER), untypedType());
    assertResolvedVar(dynamicArray, openArray);
    assertResolvedVar(type(INTEGER), openArray);
    assertResolvedVar(factory.untypedPointer(), factory.untypedPointer());
    assertResolvedVar(
        factory.pointerTo(null, type(INTEGER)), factory.pointerTo(null, type(SHORTINT)));
    assertResolvedVar(factory.fileOf(type(INTEGER)), factory.untypedFile());
  }
}
