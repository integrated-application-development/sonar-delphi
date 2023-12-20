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
package au.com.integradev.delphi.type.intrinsic;

import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_32_BIT_INTEGER;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_CLASS_REFERENCE;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_DYNAMIC_ARRAY;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_FILE;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_OBJECT;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_ORDINAL;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_SET;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_STRING;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_TEXT_FILE;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_TYPED_POINTER;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.LIKE_DYNAMIC_ARRAY;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.POINTER_MATH_OPERAND;
import static org.assertj.core.api.Assertions.assertThat;

import au.com.integradev.delphi.type.factory.ArrayOption;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import au.com.integradev.delphi.utils.types.TypeMocker;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.StructKind;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.IntegerType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class IntrinsicArgumentMatcherTest {
  private static final TypeFactoryImpl FACTORY =
      (TypeFactoryImpl) TypeFactoryUtils.defaultFactory();

  @Test
  void testLikeDynamicArray() {
    Type dynamicArray =
        FACTORY.array("TFoo", TypeFactory.untypedType(), Set.of(ArrayOption.DYNAMIC));
    Type fixedArray = FACTORY.array("TBar", TypeFactory.untypedType(), Set.of(ArrayOption.FIXED));
    Type arrayConstructor =
        FACTORY.arrayConstructor(List.of(FACTORY.getIntrinsic(IntrinsicType.INTEGER)));

    assertThat(matches(LIKE_DYNAMIC_ARRAY, dynamicArray)).isTrue();
    assertThat(matches(LIKE_DYNAMIC_ARRAY, arrayConstructor)).isTrue();
    assertThat(matches(LIKE_DYNAMIC_ARRAY, FACTORY.getIntrinsic(IntrinsicType.ANSICHAR))).isTrue();
    assertThat(matches(LIKE_DYNAMIC_ARRAY, FACTORY.getIntrinsic(IntrinsicType.WIDECHAR))).isTrue();
    assertThat(matches(LIKE_DYNAMIC_ARRAY, FACTORY.getIntrinsic(IntrinsicType.SHORTSTRING)))
        .isTrue();
    assertThat(matches(LIKE_DYNAMIC_ARRAY, FACTORY.getIntrinsic(IntrinsicType.ANSISTRING)))
        .isTrue();
    assertThat(matches(LIKE_DYNAMIC_ARRAY, FACTORY.getIntrinsic(IntrinsicType.UNICODESTRING)))
        .isTrue();
    assertThat(matches(LIKE_DYNAMIC_ARRAY, fixedArray)).isFalse();
    assertThat(matches(LIKE_DYNAMIC_ARRAY, FACTORY.emptySet())).isFalse();
  }

  @Test
  void testAnyDynamicArray() {
    assertThat(
            matches(
                ANY_DYNAMIC_ARRAY,
                FACTORY.array("TFoo", TypeFactory.untypedType(), Set.of(ArrayOption.DYNAMIC))))
        .isTrue();
    assertThat(
            matches(
                ANY_DYNAMIC_ARRAY,
                FACTORY.array("TBar", TypeFactory.untypedType(), Set.of(ArrayOption.FIXED))))
        .isFalse();
    assertThat(matches(ANY_DYNAMIC_ARRAY, FACTORY.set(TypeFactory.untypedType()))).isFalse();
  }

  @Test
  void testAnyString() {
    assertThat(matches(ANY_STRING, FACTORY.getIntrinsic(IntrinsicType.ANSICHAR))).isTrue();
    assertThat(matches(ANY_STRING, FACTORY.getIntrinsic(IntrinsicType.WIDECHAR))).isTrue();
    assertThat(matches(ANY_STRING, FACTORY.getIntrinsic(IntrinsicType.SHORTSTRING))).isTrue();
    assertThat(matches(ANY_STRING, FACTORY.getIntrinsic(IntrinsicType.ANSISTRING))).isTrue();
    assertThat(matches(ANY_STRING, FACTORY.getIntrinsic(IntrinsicType.UNICODESTRING))).isTrue();
    assertThat(matches(ANY_STRING, FACTORY.getIntrinsic(IntrinsicType.PANSICHAR))).isFalse();
    assertThat(matches(ANY_STRING, FACTORY.getIntrinsic(IntrinsicType.PCHAR))).isFalse();
    assertThat(matches(ANY_STRING, FACTORY.getIntrinsic(IntrinsicType.INTEGER))).isFalse();
  }

  @Test
  void testAnyFile() {
    assertThat(matches(ANY_FILE, FACTORY.untypedFile())).isTrue();
    assertThat(matches(ANY_FILE, FACTORY.getIntrinsic(IntrinsicType.TEXT))).isTrue();
    assertThat(matches(ANY_FILE, FACTORY.fileOf(FACTORY.getIntrinsic(IntrinsicType.INTEGER))))
        .isTrue();
    assertThat(matches(ANY_FILE, FACTORY.getIntrinsic(IntrinsicType.INTEGER))).isFalse();
  }

  @Test
  void testAnyTextFile() {
    assertThat(matches(ANY_TEXT_FILE, FACTORY.untypedFile())).isFalse();
    assertThat(matches(ANY_TEXT_FILE, FACTORY.getIntrinsic(IntrinsicType.TEXT))).isTrue();
    assertThat(matches(ANY_TEXT_FILE, FACTORY.fileOf(FACTORY.getIntrinsic(IntrinsicType.INTEGER))))
        .isFalse();
    assertThat(matches(ANY_TEXT_FILE, FACTORY.getIntrinsic(IntrinsicType.INTEGER))).isFalse();
  }

  @Test
  void testAnySet() {
    assertThat(matches(ANY_SET, FACTORY.emptySet())).isTrue();
    assertThat(matches(ANY_SET, FACTORY.arrayConstructor(Collections.emptyList()))).isTrue();
    assertThat(
            matches(
                ANY_SET,
                FACTORY.array("TFoo", TypeFactory.untypedType(), Set.of(ArrayOption.DYNAMIC))))
        .isFalse();
  }

  @Test
  void testAnyObject() {
    assertThat(matches(ANY_OBJECT, TypeMocker.struct("TFoo", StructKind.OBJECT))).isTrue();
    assertThat(matches(ANY_OBJECT, TypeMocker.struct("TBar", StructKind.CLASS))).isFalse();
  }

  @Test
  void testAnyOrdinal() {
    assertThat(matches(ANY_ORDINAL, FACTORY.getIntrinsic(IntrinsicType.INTEGER))).isTrue();
    assertThat(matches(ANY_ORDINAL, FACTORY.getIntrinsic(IntrinsicType.BOOLEAN))).isTrue();
    assertThat(matches(ANY_ORDINAL, FACTORY.enumeration("TFoo", null))).isTrue();
    assertThat(matches(ANY_ORDINAL, FACTORY.getIntrinsic(IntrinsicType.CHAR))).isTrue();
    assertThat(matches(ANY_ORDINAL, FACTORY.getIntrinsic(IntrinsicType.STRING))).isFalse();
  }

  @Test
  void testAny32BitInteger() {
    IntegerType u16 = ((IntegerType) FACTORY.getIntrinsic(IntrinsicType.WORD));
    Type subrange16 = FACTORY.subrange("TFoo", BigInteger.ZERO, u16.max());
    Type subrange32 = FACTORY.subrange("TFoo", BigInteger.ZERO, u16.max().add(BigInteger.ONE));

    assertThat(matches(ANY_32_BIT_INTEGER, FACTORY.getIntrinsic(IntrinsicType.INTEGER))).isTrue();
    assertThat(matches(ANY_32_BIT_INTEGER, FACTORY.getIntrinsic(IntrinsicType.CARDINAL))).isTrue();
    assertThat(matches(ANY_32_BIT_INTEGER, subrange32)).isTrue();
    assertThat(matches(ANY_32_BIT_INTEGER, subrange16)).isFalse();
    assertThat(matches(ANY_32_BIT_INTEGER, FACTORY.getIntrinsic(IntrinsicType.STRING))).isFalse();
  }

  @Test
  void testAnyTypedPointer() {
    Type typedPointer = FACTORY.pointerTo("PInteger", FACTORY.getIntrinsic(IntrinsicType.INTEGER));
    assertThat(matches(ANY_TYPED_POINTER, typedPointer)).isTrue();
    assertThat(matches(ANY_TYPED_POINTER, FACTORY.untypedPointer())).isFalse();
    assertThat(matches(ANY_TYPED_POINTER, FACTORY.nilPointer())).isFalse();
  }

  @Test
  void testAnyClassReference() {
    Type type = TypeMocker.struct("TFoo", StructKind.CLASS);
    assertThat(matches(ANY_CLASS_REFERENCE, type)).isFalse();
    assertThat(matches(ANY_CLASS_REFERENCE, FACTORY.classOf(null, type))).isTrue();
  }

  @Test
  void testPointerMathOperand() {
    assertThat(matches(POINTER_MATH_OPERAND, FACTORY.untypedPointer())).isTrue();
    assertThat(
            matches(
                POINTER_MATH_OPERAND,
                FACTORY.array(
                    null, FACTORY.getIntrinsic(IntrinsicType.ANSICHAR), Set.of(ArrayOption.FIXED))))
        .isTrue();
    assertThat(
            matches(
                POINTER_MATH_OPERAND,
                FACTORY.array(
                    null, FACTORY.getIntrinsic(IntrinsicType.WIDECHAR), Set.of(ArrayOption.FIXED))))
        .isTrue();
    assertThat(
            matches(
                POINTER_MATH_OPERAND,
                FACTORY.array(
                    null, FACTORY.getIntrinsic(IntrinsicType.STRING), Set.of(ArrayOption.FIXED))))
        .isFalse();
    assertThat(
            matches(
                POINTER_MATH_OPERAND,
                FACTORY.array(
                    null,
                    FACTORY.getIntrinsic(IntrinsicType.ANSICHAR),
                    Set.of(ArrayOption.DYNAMIC))))
        .isFalse();
    assertThat(
            matches(
                POINTER_MATH_OPERAND,
                FACTORY.array(
                    null,
                    FACTORY.getIntrinsic(IntrinsicType.WIDECHAR),
                    Set.of(ArrayOption.DYNAMIC))))
        .isFalse();
  }

  @Test
  void testSize() {
    assertThat(ANY_ORDINAL.size()).isZero();
  }

  private static boolean matches(Type matcher, Type type) {
    return ((IntrinsicArgumentMatcher) matcher).matches(type);
  }
}
