/*
 * Sonar Delphi Plugin
 * Copyright (C) 2026 Integrated Application Development
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

import static au.com.integradev.delphi.utils.types.TypeMocker.struct;
import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.plugins.communitydelphi.api.type.StructKind.CLASS;
import static org.sonar.plugins.communitydelphi.api.type.StructKind.INTERFACE;

import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;

class ArrayElementInferredTypeResolverTest {
  private static final TypeFactoryImpl FACTORY =
      (TypeFactoryImpl) TypeFactoryUtils.defaultFactory();
  private static final ArrayElementInferredTypeResolver RESOLVER =
      new ArrayElementInferredTypeResolver(FACTORY);

  @ParameterizedTest
  @CsvSource({
    "BYTE, BYTE",
    "SHORTINT, SHORTINT",
    "SMALLINT, SMALLINT",
    "WORD, WORD",
    "UINT64, UINT64",
    "ANSICHAR, ANSICHAR",
    "WIDECHAR, WIDECHAR",
    "VARIANT, VARIANT",
  })
  void testIdenticalElementsKeepTheirType(IntrinsicType element, IntrinsicType expected) {
    assertThat(resolve(intrinsic(element), intrinsic(element))).isEqualTo(intrinsic(expected));
  }

  @ParameterizedTest
  @CsvSource({
    "BYTE, SHORTINT, INTEGER",
    "SMALLINT, BYTE, INTEGER",
    "WORD, SHORTINT, INTEGER",
    "BYTE, INTEGER, INTEGER",
    "CARDINAL, INTEGER, INT64",
    "INTEGER, INT64, INT64",
    "CARDINAL, INT64, INT64",
    "CARDINAL, UINT64, UINT64",
  })
  void testMixedIntegerElementsPromoteToAtLeastInteger(
      IntrinsicType then, IntrinsicType els, IntrinsicType expected) {
    assertThat(resolve(intrinsic(then), intrinsic(els))).isEqualTo(intrinsic(expected));
    assertThat(resolve(intrinsic(els), intrinsic(then))).isEqualTo(intrinsic(expected));
  }

  @Test
  void testInt64AndUInt64ElementsResolveToUInt64() {
    assertThat(resolve(intrinsic(IntrinsicType.INT64), intrinsic(IntrinsicType.UINT64)))
        .isEqualTo(intrinsic(IntrinsicType.UINT64));
    assertThat(resolve(intrinsic(IntrinsicType.UINT64), intrinsic(IntrinsicType.INT64)))
        .isEqualTo(intrinsic(IntrinsicType.UINT64));
  }

  @ParameterizedTest
  @CsvSource({"SINGLE", "DOUBLE", "EXTENDED", "CURRENCY", "COMP"})
  void testRealElementsResolveToExtended(IntrinsicType real) {
    assertThat(resolve(intrinsic(real))).isEqualTo(intrinsic(IntrinsicType.EXTENDED));
    assertThat(resolve(intrinsic(real), intrinsic(real)))
        .isEqualTo(intrinsic(IntrinsicType.EXTENDED));
    assertThat(resolve(intrinsic(IntrinsicType.SINGLE), intrinsic(real)))
        .isEqualTo(intrinsic(IntrinsicType.EXTENDED));
  }

  @Test
  void testIntegerAndRealElementsResolveToUnknown() {
    assertThat(resolve(intrinsic(IntrinsicType.INTEGER), intrinsic(IntrinsicType.DOUBLE)))
        .matches(Type::isUnknown);
    assertThat(resolve(intrinsic(IntrinsicType.SINGLE), intrinsic(IntrinsicType.INTEGER)))
        .matches(Type::isUnknown);
  }

  @ParameterizedTest
  @CsvSource({
    "WIDECHAR, ANSICHAR, UNICODESTRING",
    "WIDECHAR, UNICODESTRING, UNICODESTRING",
    "UNICODESTRING, ANSISTRING, UNICODESTRING",
    "ANSICHAR, UNICODESTRING, UNICODESTRING",
  })
  void testMixedTextualElementsResolveToUnicodeString(
      IntrinsicType then, IntrinsicType els, IntrinsicType expected) {
    assertThat(resolve(intrinsic(then), intrinsic(els))).isEqualTo(intrinsic(expected));
    assertThat(resolve(intrinsic(els), intrinsic(then))).isEqualTo(intrinsic(expected));
  }

  @ParameterizedTest
  @CsvSource({
    "BOOLEAN, BYTEBOOL",
    "BYTEBOOL, BOOLEAN",
    "LONGBOOL, BOOLEAN",
    "WORDBOOL, LONGBOOL",
  })
  void testMixedBooleanElementsResolveToBoolean(IntrinsicType then, IntrinsicType els) {
    assertThat(resolve(intrinsic(then), intrinsic(els)))
        .isEqualTo(intrinsic(IntrinsicType.BOOLEAN));
  }

  @Test
  void testEnumAndItsSubrangeElementsResolveToTheEnum() {
    Type enumeration = FACTORY.enumeration("TEnum", DelphiScope.unknownScope());
    Type subrange = FACTORY.subrange("eOne..eTwo", enumeration);

    assertThat(resolve(enumeration, subrange)).isEqualTo(enumeration);
    assertThat(resolve(subrange, enumeration)).isEqualTo(enumeration);
  }

  @Test
  void testDistinctEnumElementsResolveToUnknown() {
    Type enumA = FACTORY.enumeration("TEnumA", DelphiScope.unknownScope());
    Type enumB = FACTORY.enumeration("TEnumB", DelphiScope.unknownScope());

    assertThat(resolve(enumA, enumB)).matches(Type::isUnknown);
    assertThat(resolve(enumA, intrinsic(IntrinsicType.BYTE))).matches(Type::isUnknown);
  }

  @Test
  void testClassElementsResolveToTheAssignableClassOrTheRoot() {
    StructType object = struct("TObject", CLASS);
    StructType base = struct("TBase", CLASS, object);
    StructType dog = struct("TDog", CLASS, base);
    StructType cat = struct("TCat", CLASS, base);

    assertThat(resolve(dog, base)).isEqualTo(base);
    assertThat(resolve(base, dog)).isEqualTo(base);
    assertThat(resolve(dog, cat)).isEqualTo(object);
    assertThat(resolve(cat, dog)).isEqualTo(object);
  }

  @Test
  void testClassElementOrderDecidesTheResult() {
    StructType object = struct("TObject", CLASS);
    StructType base = struct("TBase", CLASS, object);
    StructType dog = struct("TDog", CLASS, base);
    StructType cat = struct("TCat", CLASS, base);

    assertThat(RESOLVER.elementType(List.of(dog, base, cat))).isEqualTo(base);
    assertThat(RESOLVER.elementType(List.of(dog, cat, base))).isEqualTo(object);
  }

  @Test
  void testInterfaceElementsResolveToTheAssignableInterfaceOrTheRoot() {
    StructType iinterface = struct("IInterface", INTERFACE);
    StructType foo = struct("IFoo", INTERFACE, iinterface);
    StructType bar = struct("IBar", INTERFACE, foo);
    StructType baz = struct("IBaz", INTERFACE, foo);

    assertThat(resolve(bar, foo)).isEqualTo(foo);
    assertThat(resolve(bar, baz)).isEqualTo(iinterface);
  }

  @Test
  void testClassAndInterfaceElementsResolveToUnknown() {
    StructType object = struct("TObject", CLASS);
    StructType clazz = struct("TFoo", CLASS, object);
    StructType intf = struct("IFoo", INTERFACE, struct("IInterface", INTERFACE));

    assertThat(resolve(clazz, intf)).matches(Type::isUnknown);
    assertThat(resolve(intf, clazz)).matches(Type::isUnknown);
  }

  @Test
  void testClassReferenceElementsResolveToTheAncestorReference() {
    StructType object = struct("TObject", CLASS);
    StructType base = struct("TBase", CLASS, object);
    StructType dog = struct("TDog", CLASS, base);
    Type baseReference = FACTORY.classOf("TBaseClass", base);
    Type dogReference = FACTORY.classOf("TDogClass", dog);

    assertThat(resolve(baseReference, dogReference)).isEqualTo(baseReference);
    assertThat(resolve(dogReference, baseReference)).isEqualTo(baseReference);
  }

  @Test
  void testUnrelatedClassReferenceElementsResolveToUnknown() {
    Type left = FACTORY.classOf("TFooClass", struct("TFoo", CLASS));
    Type right = FACTORY.classOf("TBarClass", struct("TBar", CLASS));

    assertThat(resolve(left, right)).matches(Type::isUnknown);
  }

  @Test
  void testPointerElementsResolveToPointer() {
    Type pInteger = FACTORY.pointerTo("PInteger", intrinsic(IntrinsicType.INTEGER));
    Type pByte = FACTORY.pointerTo("PByte", intrinsic(IntrinsicType.BYTE));
    StructType clazz = struct("TFoo", CLASS, struct("TObject", CLASS));

    assertThat(resolve(pInteger, pByte)).isEqualTo(intrinsic(IntrinsicType.POINTER));
    assertThat(resolve(pInteger, FACTORY.nilPointer())).isEqualTo(intrinsic(IntrinsicType.POINTER));
    assertThat(resolve(FACTORY.nilPointer())).isEqualTo(intrinsic(IntrinsicType.POINTER));
    assertThat(resolve(clazz, FACTORY.nilPointer())).matches(Type::isUnknown);
  }

  @Test
  void testVariantAndOtherElementsResolveToUnknown() {
    assertThat(resolve(intrinsic(IntrinsicType.VARIANT), intrinsic(IntrinsicType.INTEGER)))
        .matches(Type::isUnknown);
    assertThat(resolve(intrinsic(IntrinsicType.VARIANT), intrinsic(IntrinsicType.UNICODESTRING)))
        .matches(Type::isUnknown);
  }

  @Test
  void testCrossFamilyElementsResolveToUnknown() {
    assertThat(resolve(intrinsic(IntrinsicType.UNICODESTRING), intrinsic(IntrinsicType.INTEGER)))
        .matches(Type::isUnknown);
    assertThat(resolve(intrinsic(IntrinsicType.BOOLEAN), intrinsic(IntrinsicType.INTEGER)))
        .matches(Type::isUnknown);
    assertThat(
            resolve(
                FACTORY.pointerTo("PInteger", intrinsic(IntrinsicType.INTEGER)),
                intrinsic(IntrinsicType.INTEGER)))
        .matches(Type::isUnknown);
    assertThat(
            resolve(
                FACTORY.classOf("TFooClass", struct("TFoo", CLASS)),
                intrinsic(IntrinsicType.INTEGER)))
        .matches(Type::isUnknown);
  }

  @Test
  void testNoElementsResolveToUnknown() {
    assertThat(RESOLVER.elementType(List.of())).matches(Type::isUnknown);
  }

  @Test
  void testFailedUnificationIsAbsorbing() {
    assertThat(
            RESOLVER.elementType(
                List.of(
                    intrinsic(IntrinsicType.INTEGER),
                    intrinsic(IntrinsicType.DOUBLE),
                    intrinsic(IntrinsicType.INTEGER))))
        .matches(Type::isUnknown);
  }

  private static Type resolve(Type... elements) {
    return RESOLVER.elementType(List.of(elements));
  }

  private static Type intrinsic(IntrinsicType type) {
    return FACTORY.getIntrinsic(type);
  }
}
