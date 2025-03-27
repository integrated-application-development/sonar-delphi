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
package au.com.integradev.delphi.type.intrinsic;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

import au.com.integradev.delphi.compiler.CompilerVersion;
import au.com.integradev.delphi.compiler.Toolchain;
import au.com.integradev.delphi.type.factory.ArrayOption;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ArrayConstructorType;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class IntrinsicReturnTypeTest {
  private static final TypeFactory TYPE_FACTORY_32_ALEXANDRIA =
      new TypeFactoryImpl(Toolchain.DCC32, CompilerVersion.fromVersionNumber("35.0"));
  private static final TypeFactory TYPE_FACTORY_32_ATHENS =
      new TypeFactoryImpl(Toolchain.DCC32, CompilerVersion.fromVersionNumber("36.0"));
  private static final TypeFactory TYPE_FACTORY_64_ALEXANDRIA =
      new TypeFactoryImpl(Toolchain.DCC64, CompilerVersion.fromVersionNumber("35.0"));
  private static final TypeFactory TYPE_FACTORY_64_ATHENS =
      new TypeFactoryImpl(Toolchain.DCC64, CompilerVersion.fromVersionNumber("36.0"));

  @Test
  void testLength() {
    Type shortString = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.SHORTSTRING);
    Type ansiString = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.ANSISTRING);
    Type wideString = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.WIDESTRING);
    Type unicodeString = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.UNICODESTRING);
    Type fixedArray =
        ((TypeFactoryImpl) TYPE_FACTORY_64_ALEXANDRIA)
            .array(null, ansiString, Set.of(ArrayOption.FIXED));
    Type dynamicArray =
        ((TypeFactoryImpl) TYPE_FACTORY_64_ALEXANDRIA)
            .array(null, ansiString, Set.of(ArrayOption.DYNAMIC));
    Type openArray =
        ((TypeFactoryImpl) TYPE_FACTORY_64_ALEXANDRIA)
            .array(null, ansiString, Set.of(ArrayOption.OPEN));

    var rio32 = (IntrinsicReturnType) IntrinsicReturnType.length(TYPE_FACTORY_32_ALEXANDRIA);
    assertThat(rio32.getReturnType(List.of(shortString)).is(IntrinsicType.BYTE)).isTrue();
    assertThat(rio32.getReturnType(List.of(ansiString)).is(IntrinsicType.INTEGER)).isTrue();
    assertThat(rio32.getReturnType(List.of(wideString)).is(IntrinsicType.INTEGER)).isTrue();
    assertThat(rio32.getReturnType(List.of(unicodeString)).is(IntrinsicType.INTEGER)).isTrue();
    assertThat(rio32.getReturnType(List.of(fixedArray)).is(IntrinsicType.INTEGER)).isTrue();
    assertThat(rio32.getReturnType(List.of(dynamicArray)).is(IntrinsicType.INTEGER)).isTrue();
    assertThat(rio32.getReturnType(List.of(openArray)).is(IntrinsicType.INTEGER)).isTrue();

    var rio64 = (IntrinsicReturnType) IntrinsicReturnType.length(TYPE_FACTORY_64_ALEXANDRIA);
    assertThat(rio64.getReturnType(List.of(fixedArray)).is(IntrinsicType.INTEGER)).isTrue();
    assertThat(rio64.getReturnType(List.of(dynamicArray)).is(IntrinsicType.NATIVEINT)).isTrue();
    assertThat(rio64.getReturnType(List.of(openArray)).is(IntrinsicType.INTEGER)).isTrue();

    var alex32 = (IntrinsicReturnType) IntrinsicReturnType.length(TYPE_FACTORY_32_ATHENS);
    assertThat(alex32.getReturnType(List.of(shortString)).is(IntrinsicType.BYTE)).isTrue();
    assertThat(alex32.getReturnType(List.of(ansiString)).is(IntrinsicType.INTEGER)).isTrue();
    assertThat(alex32.getReturnType(List.of(wideString)).is(IntrinsicType.INTEGER)).isTrue();
    assertThat(alex32.getReturnType(List.of(unicodeString)).is(IntrinsicType.INTEGER)).isTrue();
    assertThat(alex32.getReturnType(List.of(fixedArray)).is(IntrinsicType.INTEGER)).isTrue();
    assertThat(alex32.getReturnType(List.of(dynamicArray)).is(IntrinsicType.INTEGER)).isTrue();
    assertThat(alex32.getReturnType(List.of(openArray)).is(IntrinsicType.INTEGER)).isTrue();

    var alex64 = (IntrinsicReturnType) IntrinsicReturnType.length(TYPE_FACTORY_64_ATHENS);
    assertThat(alex64.getReturnType(List.of(fixedArray)).is(IntrinsicType.INTEGER)).isTrue();
    assertThat(alex64.getReturnType(List.of(dynamicArray)).is(IntrinsicType.NATIVEINT)).isTrue();
    assertThat(alex64.getReturnType(List.of(openArray)).is(IntrinsicType.NATIVEINT)).isTrue();
  }

  @Test
  void testHighLow() {
    Type smallInt = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.SMALLINT);
    Type integer = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.INTEGER);
    Type string = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.UNICODESTRING);
    Type fixedArray =
        ((TypeFactoryImpl) TYPE_FACTORY_64_ALEXANDRIA)
            .array(null, string, Set.of(ArrayOption.FIXED));
    Type dynamicArray =
        ((TypeFactoryImpl) TYPE_FACTORY_64_ALEXANDRIA)
            .array(null, string, Set.of(ArrayOption.DYNAMIC));
    Type openArray =
        ((TypeFactoryImpl) TYPE_FACTORY_64_ALEXANDRIA)
            .array(null, string, Set.of(ArrayOption.OPEN));
    Type classType = mock(StructType.class);
    Type classReference = TYPE_FACTORY_64_ALEXANDRIA.classOf("Foo", classType);

    var rio32 = (IntrinsicReturnType) IntrinsicReturnType.high(TYPE_FACTORY_32_ALEXANDRIA);
    assertThat(rio32.getReturnType(List.of(smallInt)).is(smallInt)).isTrue();
    assertThat(rio32.getReturnType(List.of(integer)).is(integer)).isTrue();
    assertThat(rio32.getReturnType(List.of(string)).is(integer)).isTrue();
    assertThat(rio32.getReturnType(List.of(fixedArray)).is(integer)).isTrue();
    assertThat(rio32.getReturnType(List.of(dynamicArray)).is(integer)).isTrue();
    assertThat(rio32.getReturnType(List.of(openArray)).is(integer)).isTrue();
    assertThat(rio32.getReturnType(List.of(classReference))).isSameAs(classType);

    var rio64 = (IntrinsicReturnType) IntrinsicReturnType.high(TYPE_FACTORY_64_ALEXANDRIA);
    assertThat(rio64.getReturnType(List.of(fixedArray)).is(integer)).isTrue();
    assertThat(rio64.getReturnType(List.of(dynamicArray)).is(IntrinsicType.NATIVEINT)).isTrue();
    assertThat(rio64.getReturnType(List.of(openArray)).is(integer)).isTrue();

    var alex32 = (IntrinsicReturnType) IntrinsicReturnType.high(TYPE_FACTORY_32_ATHENS);
    assertThat(alex32.getReturnType(List.of(smallInt)).is(smallInt)).isTrue();
    assertThat(alex32.getReturnType(List.of(integer)).is(integer)).isTrue();
    assertThat(alex32.getReturnType(List.of(string)).is(integer)).isTrue();
    assertThat(alex32.getReturnType(List.of(fixedArray)).is(integer)).isTrue();
    assertThat(alex32.getReturnType(List.of(dynamicArray)).is(integer)).isTrue();
    assertThat(alex32.getReturnType(List.of(openArray)).is(integer)).isTrue();
    assertThat(alex32.getReturnType(List.of(classReference))).isSameAs(classType);

    var alex64 = (IntrinsicReturnType) IntrinsicReturnType.high(TYPE_FACTORY_64_ATHENS);
    assertThat(alex64.getReturnType(List.of(fixedArray)).is(integer)).isTrue();
    assertThat(alex64.getReturnType(List.of(dynamicArray)).is(IntrinsicType.NATIVEINT)).isTrue();
    assertThat(alex64.getReturnType(List.of(openArray)).is(IntrinsicType.NATIVEINT)).isTrue();
  }

  @Test
  void testRoundTrunc() {
    Type single = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.SINGLE);
    Type int64 = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.INT64);
    var round = (IntrinsicReturnType) IntrinsicReturnType.round(TYPE_FACTORY_64_ALEXANDRIA);
    assertThat(round.getReturnType(List.of(single)).is(int64)).isTrue();
  }

  @Test
  void testClassReferenceValueType() {
    Type classType = mock(StructType.class);
    Type classReference = TYPE_FACTORY_64_ALEXANDRIA.classOf("Foo", classType);

    var classReferenceValue = (IntrinsicReturnType) IntrinsicReturnType.classReferenceValue(0);
    assertThat(classReferenceValue.getReturnType(List.of(classReference))).isSameAs(classType);
    assertThat(classReferenceValue.getReturnType(List.of(classType)).isUnknown()).isTrue();
  }

  @Test
  void testConcat() {
    Type ansiString = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.ANSISTRING);
    Type string = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.UNICODESTRING);
    Type stringAlias = TYPE_FACTORY_64_ALEXANDRIA.strongAlias("StringAlias", string);
    Type stringAlias2 = TYPE_FACTORY_64_ALEXANDRIA.strongAlias("StringAlias2", stringAlias);
    Type ansiChar = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.ANSICHAR);
    Type wideChar = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.WIDECHAR);
    Type array =
        ((TypeFactoryImpl) TYPE_FACTORY_64_ALEXANDRIA)
            .array(null, string, Set.of(ArrayOption.DYNAMIC));
    Type arrayConstructor = TYPE_FACTORY_64_ALEXANDRIA.arrayConstructor(List.of(string));
    Type variant = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.VARIANT);
    Type oleVariant = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.OLEVARIANT);

    var concat = (IntrinsicReturnType) IntrinsicReturnType.concat(TYPE_FACTORY_64_ALEXANDRIA);
    assertThat(concat.getReturnType(List.of(ansiString, ansiString)).is(ansiString)).isTrue();
    assertThat(concat.getReturnType(List.of(ansiChar, ansiString)).is(ansiString)).isTrue();
    assertThat(concat.getReturnType(List.of(string, string)).is(string)).isTrue();
    assertThat(concat.getReturnType(List.of(wideChar, string)).is(string)).isTrue();
    assertThat(concat.getReturnType(List.of(ansiString, string)).is(string)).isTrue();
    assertThat(concat.getReturnType(List.of(ansiChar, string)).is(string)).isTrue();
    assertThat(concat.getReturnType(List.of(ansiChar, string)).is(string)).isTrue();
    assertThat(concat.getReturnType(List.of(stringAlias, stringAlias)).is(string)).isTrue();
    assertThat(concat.getReturnType(List.of(stringAlias2, stringAlias2)).is(string)).isTrue();
    assertThat(concat.getReturnType(List.of(array, array)).is(array)).isTrue();
    assertThat(concat.getReturnType(List.of(arrayConstructor, array)).is(array)).isTrue();

    var arrayConstructorReturnType =
        (ArrayConstructorType)
            concat.getReturnType(List.of(arrayConstructor, arrayConstructor, arrayConstructor));
    assertThat(arrayConstructorReturnType.elementTypes()).hasSize(3);

    assertThat(concat.getReturnType(List.of(variant, variant)).is(variant)).isTrue();
    assertThat(concat.getReturnType(List.of(oleVariant, oleVariant)).is(oleVariant)).isTrue();
    assertThat(concat.getReturnType(List.of(variant, oleVariant)).is(variant)).isTrue();
    assertThat(concat.getReturnType(List.of(oleVariant, variant)).is(oleVariant)).isTrue();
  }

  @Test
  void testCopy() {
    Type ansiString = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.ANSISTRING);
    Type string = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.UNICODESTRING);
    Type stringAlias = TYPE_FACTORY_64_ALEXANDRIA.strongAlias("StringAlias", string);
    Type ansiChar = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.ANSICHAR);
    Type wideChar = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.WIDECHAR);
    Type array =
        ((TypeFactoryImpl) TYPE_FACTORY_64_ALEXANDRIA)
            .array(null, string, Set.of(ArrayOption.DYNAMIC));
    Type arrayConstructor = TYPE_FACTORY_64_ALEXANDRIA.arrayConstructor(List.of(string));
    Type variant = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.VARIANT);
    Type oleVariant = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.OLEVARIANT);

    var copy = (IntrinsicReturnType) IntrinsicReturnType.copy(TYPE_FACTORY_64_ALEXANDRIA);
    assertThat(copy.getReturnType(List.of(ansiString)).is(ansiString)).isTrue();
    assertThat(copy.getReturnType(List.of(string)).is(string)).isTrue();
    assertThat(copy.getReturnType(List.of(stringAlias)).is(stringAlias)).isTrue();
    assertThat(copy.getReturnType(List.of(ansiChar)).is(ansiString)).isTrue();
    assertThat(copy.getReturnType(List.of(wideChar)).is(string)).isTrue();
    assertThat(copy.getReturnType(List.of(array)).is(array)).isTrue();
    assertThat(copy.getReturnType(List.of(arrayConstructor)).is(arrayConstructor)).isTrue();
    assertThat(copy.getReturnType(List.of(variant)).is(string)).isTrue();
    assertThat(copy.getReturnType(List.of(oleVariant)).is(string)).isTrue();
  }

  @Test
  void testSlice() {
    Type string = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.UNICODESTRING);
    Type array =
        ((TypeFactoryImpl) TYPE_FACTORY_64_ALEXANDRIA)
            .array(null, string, Set.of(ArrayOption.DYNAMIC));
    Type arrayConstructor = TYPE_FACTORY_64_ALEXANDRIA.arrayConstructor(List.of(string));

    var slice = (IntrinsicReturnType) IntrinsicReturnType.slice(TYPE_FACTORY_64_ALEXANDRIA);
    assertThat(slice.getReturnType(List.of(array)).isOpenArray()).isTrue();
    assertThat(slice.getReturnType(List.of(string)).isUnknown()).isTrue();
    assertThat(slice.getReturnType(List.of(arrayConstructor)).isUnknown()).isTrue();
  }

  @Test
  void testArgumentByIndex() {
    Type shortInt = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.SHORTINT);
    Type smallInt = TYPE_FACTORY_64_ALEXANDRIA.getIntrinsic(IntrinsicType.SMALLINT);

    var argumentByIndex0 = (IntrinsicReturnType) IntrinsicReturnType.argumentByIndex(0);
    assertThat(argumentByIndex0.getReturnType(List.of(shortInt, smallInt)).is(shortInt)).isTrue();

    var argumentByIndex1 = (IntrinsicReturnType) IntrinsicReturnType.argumentByIndex(1);
    assertThat(argumentByIndex1.getReturnType(List.of(shortInt, smallInt)).is(smallInt)).isTrue();
  }
}
