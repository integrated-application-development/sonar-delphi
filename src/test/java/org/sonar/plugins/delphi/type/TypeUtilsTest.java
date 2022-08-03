package org.sonar.plugins.delphi.type;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.type.factory.TypeFactory;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicType;
import org.sonar.plugins.delphi.utils.types.TypeFactoryUtils;

class TypeUtilsTest {
  private static final TypeFactory FACTORY = TypeFactoryUtils.defaultFactory();

  @Test
  void testFindBaseType() {
    Type baseType = FACTORY.getIntrinsic(IntrinsicType.INTEGER);
    assertThat(TypeUtils.findBaseType(baseType)).isEqualTo(baseType);

    Type typeType = FACTORY.typeType("String_", baseType);
    assertThat(TypeUtils.findBaseType(typeType)).isEqualTo(baseType);

    Type subrangeType = FACTORY.subRange("IntegerSubrange", baseType);
    assertThat(TypeUtils.findBaseType(subrangeType)).isEqualTo(baseType);
  }

  @Test
  void testDereference() {
    Type dereferenced = FACTORY.getIntrinsic(IntrinsicType.INTEGER);
    assertThat(TypeUtils.dereference(dereferenced)).isEqualTo(dereferenced);

    Type pointer = FACTORY.pointerTo(dereferenced);
    assertThat(TypeUtils.dereference(pointer)).isEqualTo(dereferenced);
  }

  @Test
  void testIsNarrowString() {
    assertThat(TypeUtils.isNarrowString(FACTORY.getIntrinsic(IntrinsicType.ANSISTRING))).isTrue();
    assertThat(TypeUtils.isNarrowString(FACTORY.getIntrinsic(IntrinsicType.WIDESTRING))).isFalse();
    assertThat(TypeUtils.isNarrowString(FACTORY.getIntrinsic(IntrinsicType.UNICODESTRING)))
        .isFalse();
    assertThat(TypeUtils.isNarrowString(FACTORY.getIntrinsic(IntrinsicType.INTEGER))).isFalse();
  }

  @Test
  void testIsWideString() {
    assertThat(TypeUtils.isWideString(FACTORY.getIntrinsic(IntrinsicType.ANSISTRING))).isFalse();
    assertThat(TypeUtils.isWideString(FACTORY.getIntrinsic(IntrinsicType.WIDESTRING))).isTrue();
    assertThat(TypeUtils.isWideString(FACTORY.getIntrinsic(IntrinsicType.UNICODESTRING))).isTrue();
    assertThat(TypeUtils.isWideString(FACTORY.getIntrinsic(IntrinsicType.INTEGER))).isFalse();
  }
}
