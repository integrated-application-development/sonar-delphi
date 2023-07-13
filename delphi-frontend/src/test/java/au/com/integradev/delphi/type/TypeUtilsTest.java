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

import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;
import org.sonar.plugins.communitydelphi.api.type.TypeUtils;

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

    Type pointer = FACTORY.pointerTo(null, dereferenced);
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
