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

import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_CLASS_REFERENCE;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_DYNAMIC_ARRAY;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_OBJECT;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_ORDINAL;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_SET;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_TYPED_POINTER;
import static au.com.integradev.delphi.type.intrinsic.IntrinsicArgumentMatcher.POINTER_MATH_OPERAND;
import static org.assertj.core.api.Assertions.assertThat;

import au.com.integradev.delphi.type.factory.ArrayOption;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import au.com.integradev.delphi.utils.types.TypeMocker;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.StructKind;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class IntrinsicArgumentMatcherTest {
  private static final TypeFactoryImpl FACTORY =
      (TypeFactoryImpl) TypeFactoryUtils.defaultFactory();

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
