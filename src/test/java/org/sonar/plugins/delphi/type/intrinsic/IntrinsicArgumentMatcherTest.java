package org.sonar.plugins.delphi.type.intrinsic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_CLASS_REFERENCE;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_DYNAMIC_ARRAY;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_OBJECT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_ORDINAL;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_SET;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.POINTER_MATH_OPERAND;

import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.type.ArrayOption;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.StructKind;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.factory.TypeFactory;
import org.sonar.plugins.delphi.utils.types.TypeFactoryUtils;
import org.sonar.plugins.delphi.utils.types.TypeMocker;

class IntrinsicArgumentMatcherTest {
  private static final TypeFactory FACTORY = TypeFactoryUtils.defaultFactory();

  @Test
  void testAnyDynamicArray() {
    assertThat(
            matches(
                ANY_DYNAMIC_ARRAY,
                FACTORY.array("TFoo", DelphiType.untypedType(), Set.of(ArrayOption.DYNAMIC))))
        .isTrue();
    assertThat(
            matches(
                ANY_DYNAMIC_ARRAY,
                FACTORY.array("TBar", DelphiType.untypedType(), Set.of(ArrayOption.FIXED))))
        .isFalse();
    assertThat(matches(ANY_DYNAMIC_ARRAY, FACTORY.set(DelphiType.untypedType()))).isFalse();
  }

  @Test
  void testAnySet() {
    assertThat(matches(ANY_SET, FACTORY.emptySet())).isTrue();
    assertThat(matches(ANY_SET, FACTORY.arrayConstructor(Collections.emptyList()))).isTrue();
    assertThat(
            matches(
                ANY_SET,
                FACTORY.array("TFoo", DelphiType.untypedType(), Set.of(ArrayOption.DYNAMIC))))
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
  void testAnyClassReference() {
    Type type = TypeMocker.struct("TFoo", StructKind.CLASS);
    assertThat(matches(ANY_CLASS_REFERENCE, type)).isFalse();
    assertThat(matches(ANY_CLASS_REFERENCE, FACTORY.classOf(type))).isTrue();
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
