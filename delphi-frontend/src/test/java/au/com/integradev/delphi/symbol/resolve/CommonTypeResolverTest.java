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
import static org.sonar.plugins.communitydelphi.api.type.TypeFactory.unknownType;

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.compiler.Toolchain;
import au.com.integradev.delphi.type.factory.ArrayOption;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import au.com.integradev.delphi.utils.types.TypeMocker;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.StructKind;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ClassReferenceType;
import org.sonar.plugins.communitydelphi.api.type.Type.CollectionType;
import org.sonar.plugins.communitydelphi.api.type.Type.IntegerType;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType.ProceduralKind;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class CommonTypeResolverTest {
  private static final TypeFactoryImpl FACTORY =
      (TypeFactoryImpl) TypeFactoryUtils.defaultFactory();
  private static final CommonTypeResolver RESOLVER = new CommonTypeResolver(FACTORY);

  private static final TypeFactoryImpl DCC64_FACTORY =
      new TypeFactoryImpl(Toolchain.DCC64, DelphiProperties.COMPILER_VERSION_DEFAULT);
  private static final CommonTypeResolver DCC64_RESOLVER = new CommonTypeResolver(DCC64_FACTORY);

  @ParameterizedTest
  @CsvSource({
    "INTEGER, INTEGER, INTEGER",
    "UNICODESTRING, UNICODESTRING, UNICODESTRING",
    "ANSISTRING, ANSISTRING, ANSISTRING",
    "WIDESTRING, WIDESTRING, WIDESTRING",
    "BOOLEAN, BOOLEAN, BOOLEAN",
    "SINGLE, SINGLE, SINGLE",
    "DOUBLE, DOUBLE, DOUBLE",
    "SHORTSTRING, SHORTSTRING, SHORTSTRING",
    "ANSICHAR, ANSICHAR, ANSICHAR",
    "WIDECHAR, WIDECHAR, WIDECHAR",
    "OLEVARIANT, OLEVARIANT, OLEVARIANT",
  })
  void testIdenticalBranchTypesResolveToThatType(
      IntrinsicType then, IntrinsicType els, IntrinsicType expected) {
    assertResolvesBothWays(then, els, expected);
  }

  /** Char is a weak alias of WideChar. */
  @Test
  void testWeakAliasBranchesResolveToTheAliasedType() {
    assertThat(resolve(intrinsic(IntrinsicType.CHAR), intrinsic(IntrinsicType.WIDECHAR)))
        .matches(type -> type.is(IntrinsicType.WIDECHAR));
    assertThat(resolve(intrinsic(IntrinsicType.WIDECHAR), intrinsic(IntrinsicType.CHAR)))
        .matches(type -> type.is(IntrinsicType.WIDECHAR));
  }

  /**
   * Unlike arithmetic, which widens every operand to at least {@code Integer}, an {@code if}
   * expression resolves to the narrowest integer type holding both branches.
   */
  @ParameterizedTest
  @CsvSource({
    "SHORTINT, BYTE, SMALLINT",
    "BYTE, WORD, WORD",
    "BYTE, SMALLINT, SMALLINT",
    "SHORTINT, SMALLINT, SMALLINT",
    "SHORTINT, WORD, INTEGER",
    "SMALLINT, WORD, INTEGER",
    "BYTE, INTEGER, INTEGER",
    "SHORTINT, INTEGER, INTEGER",
    "BYTE, CARDINAL, CARDINAL",
    "WORD, CARDINAL, CARDINAL",
    "INTEGER, CARDINAL, INT64",
    "SHORTINT, CARDINAL, INT64",
    "INTEGER, INT64, INT64",
    "CARDINAL, INT64, INT64",
    "INTEGER, UINT64, UINT64",
    "INT64, UINT64, UINT64",
    "NATIVEINT, BYTE, INTEGER",
    "NATIVEINT, INT64, INT64",
  })
  void testIntegerBranchesResolveToTheNarrowestIntegerHoldingBoth(
      IntrinsicType then, IntrinsicType els, IntrinsicType expected) {
    assertResolvesBothWays(then, els, expected);
  }

  /**
   * Where a signed and an unsigned type of the same size both hold the combined range, the compiler
   * uses an anonymous unsigned subrange, whatever the range actually is.
   */
  @ParameterizedTest
  @CsvSource({
    "0, 1000, 0, 500, 0, 32767",
    "0, 32767, 0, 255, 0, 32767",
    "0, 100000, 0, 99, 0, 2147483647",
    "0, 2147483647, 0, 99, 0, 2147483647",
  })
  void testIntegerBranchesHeldByBothSignedAndUnsignedResolveToAnAnonymousSubrange(
      int thenMin, int thenMax, int elseMin, int elseMax, int expectedMin, int expectedMax) {
    Type result = resolve(subrange(thenMin, thenMax), subrange(elseMin, elseMax));

    assertThat(result.isSubrange()).isTrue();
    assertThat(((IntegerType) result).min()).isEqualTo(BigInteger.valueOf(expectedMin));
    assertThat(((IntegerType) result).max()).isEqualTo(BigInteger.valueOf(expectedMax));
  }

  /** There is no anonymous subrange for one and eight byte results, so the signed type wins. */
  @Test
  void testIntegerBranchesWithNoAnonymousSubrangeResolveToTheSignedType() {
    assertThat(resolve(subrange(0, 100), subrange(0, 50)))
        .isEqualTo(intrinsic(IntrinsicType.SHORTINT));
    assertThat(resolve(subrange(0, Long.MAX_VALUE), subrange(0, 99)))
        .isEqualTo(intrinsic(IntrinsicType.INT64));
  }

  /** On DCC32, Extended is 10 bytes and can hold Comp and Currency without losing precision. */
  @ParameterizedTest
  @CsvSource({
    "SINGLE, DOUBLE, EXTENDED",
    "DOUBLE, EXTENDED, EXTENDED",
    "SINGLE, EXTENDED, EXTENDED",
    "INTEGER, SINGLE, EXTENDED",
    "INTEGER, DOUBLE, EXTENDED",
    "INTEGER, EXTENDED, EXTENDED",
    "INT64, EXTENDED, EXTENDED",
    "CURRENCY, EXTENDED, EXTENDED",
    "CURRENCY, SINGLE, EXTENDED",
    "COMP, SINGLE, EXTENDED",
    "COMP, DOUBLE, EXTENDED",
    "INTEGER, COMP, EXTENDED",
    "INT64, COMP, EXTENDED",
    "BYTE, COMP, EXTENDED",
    "INTEGER, CURRENCY, EXTENDED",
    "INT64, CURRENCY, EXTENDED",
    "COMP, CURRENCY, EXTENDED",
    "REAL48, DOUBLE, EXTENDED",
    "INTEGER, REAL48, EXTENDED",
  })
  void testRealBranchesWidenToExtended(
      IntrinsicType then, IntrinsicType els, IntrinsicType expected) {
    assertResolvesBothWays(then, els, expected);
  }

  /** On DCC64, Extended is 8 bytes. This causes Comp and Currency to be preferred for some */
  @ParameterizedTest
  @CsvSource({
    "INTEGER, COMP, COMP",
    "INT64, COMP, COMP",
    "BYTE, COMP, COMP",
    "INTEGER, CURRENCY, CURRENCY",
    "INT64, CURRENCY, CURRENCY",
  })
  void testFixedPointBranchesAbsorbIntegerBranchesWhereExtendedCannotHoldThem(
      IntrinsicType then, IntrinsicType els, IntrinsicType expected) {
    assertResolvesBothWays(DCC64_RESOLVER, DCC64_FACTORY, then, els, expected);
  }

  @ParameterizedTest
  @CsvSource({
    "SINGLE, DOUBLE",
    "DOUBLE, CURRENCY",
    "SINGLE, CURRENCY",
    "EXTENDED, CURRENCY",
    "DOUBLE, COMP",
    "SINGLE, COMP",
    "EXTENDED, COMP",
  })
  void testFloatingPointBranchesWidenToExtendedEvenWhereExtendedCannotHoldFixedPointTypes(
      IntrinsicType then, IntrinsicType els) {
    assertResolvesBothWays(DCC64_RESOLVER, DCC64_FACTORY, then, els, IntrinsicType.EXTENDED);
  }

  @Test
  void testCurrencyBeatsCompWhereExtendedCannotHoldThem() {
    assertResolvesBothWays(
        DCC64_RESOLVER,
        DCC64_FACTORY,
        IntrinsicType.COMP,
        IntrinsicType.CURRENCY,
        IntrinsicType.CURRENCY);
  }

  @ParameterizedTest
  @CsvSource({
    "CHAR, UNICODESTRING, UNICODESTRING",
    "ANSICHAR, ANSISTRING, ANSISTRING",
    "ANSICHAR, WIDESTRING, WIDESTRING",
    "ANSICHAR, WIDECHAR, UNICODESTRING",
    "ANSICHAR, UNICODESTRING, UNICODESTRING",
    "ANSICHAR, SHORTSTRING, SHORTSTRING",
    "WIDECHAR, ANSISTRING, UNICODESTRING",
    "WIDECHAR, WIDESTRING, WIDESTRING",
    "ANSISTRING, UNICODESTRING, UNICODESTRING",
    "ANSISTRING, WIDESTRING, WIDESTRING",
    "WIDESTRING, UNICODESTRING, UNICODESTRING",
    "SHORTSTRING, UNICODESTRING, UNICODESTRING",
  })
  void testTextualBranchesWidenToACommonTextualType(
      IntrinsicType then, IntrinsicType els, IntrinsicType expected) {
    assertResolvesBothWays(then, els, expected);
  }

  /**
   * Where neither branch type is wider than the other, the compiler keeps the type of the {@code
   * then} branch rather than picking a common type.
   */
  @ParameterizedTest
  @CsvSource({
    "BOOLEAN, BYTEBOOL",
    "BOOLEAN, WORDBOOL",
    "BOOLEAN, LONGBOOL",
    "BYTEBOOL, WORDBOOL",
    "WORDBOOL, LONGBOOL",
  })
  void testMixedBooleanBranchesResolveToTheThenBranch(IntrinsicType then, IntrinsicType els) {
    assertThat(resolve(intrinsic(then), intrinsic(els))).isEqualTo(intrinsic(then));
    assertThat(resolve(intrinsic(els), intrinsic(then))).isEqualTo(intrinsic(els));
  }

  @Test
  void testMixedAnsiStringBranchesResolveToTheThenBranch() {
    Type ansiString = intrinsic(IntrinsicType.ANSISTRING);
    Type utf8String = FACTORY.ansiString(65001);

    assertThat(resolve(ansiString, utf8String)).isEqualTo(ansiString);
    assertThat(resolve(utf8String, ansiString)).isEqualTo(utf8String);
  }

  @Test
  void testAnsiCharBranchIsAbsorbedByAnAnsiStringOfAnyCodePage() {
    Type ansiChar = intrinsic(IntrinsicType.ANSICHAR);
    Type utf8String = FACTORY.ansiString(65001);

    assertThat(resolve(ansiChar, utf8String)).isEqualTo(utf8String);
    assertThat(resolve(utf8String, ansiChar)).isEqualTo(utf8String);
  }

  @Test
  void testMixedVariantBranchesResolveToVariant() {
    assertResolvesBothWays(IntrinsicType.VARIANT, IntrinsicType.OLEVARIANT, IntrinsicType.VARIANT);
  }

  @ParameterizedTest
  @CsvSource({
    "INTEGER, UNICODESTRING",
    "INTEGER, BOOLEAN",
    "UNICODESTRING, BOOLEAN",
    "DOUBLE, UNICODESTRING",
    "INTEGER, VARIANT",
    "UNICODESTRING, VARIANT",
    "BOOLEAN, VARIANT",
    "DOUBLE, VARIANT",
    "ANSICHAR, VARIANT",
    "INTEGER, ANSICHAR",
    "BOOLEAN, ANSICHAR",
    "SHORTSTRING, ANSISTRING",
    "SHORTSTRING, WIDESTRING",
    "SHORTSTRING, WIDECHAR",
  })
  void testUnrelatedBranchesResolveToUnknown(IntrinsicType then, IntrinsicType els) {
    assertThat(resolve(intrinsic(then), intrinsic(els))).matches(Type::isUnknown);
    assertThat(resolve(intrinsic(els), intrinsic(then))).matches(Type::isUnknown);
  }

  @Test
  void testIntrinsicAndStructuralBranchesResolveToUnknown() {
    assertThat(resolve(intrinsic(IntrinsicType.INTEGER), intrinsic(IntrinsicType.POINTER)))
        .matches(Type::isUnknown);
    assertThat(resolve(intrinsic(IntrinsicType.POINTER), intrinsic(IntrinsicType.INTEGER)))
        .matches(Type::isUnknown);
  }

  @Test
  void testClassReferenceOrProceduralAgainstAnUnrelatedTypeResolvesToUnknown() {
    Type reference = FACTORY.classOf("TFooClass", struct("TFoo", CLASS));
    Type procedural = procedure(List.of(), TypeFactory.voidType());

    assertThat(resolve(reference, intrinsic(IntrinsicType.INTEGER))).matches(Type::isUnknown);
    assertThat(resolve(intrinsic(IntrinsicType.INTEGER), reference)).matches(Type::isUnknown);
    assertThat(resolve(procedural, intrinsic(IntrinsicType.INTEGER))).matches(Type::isUnknown);
    assertThat(resolve(intrinsic(IntrinsicType.INTEGER), procedural)).matches(Type::isUnknown);
  }

  @Test
  void testUnknownBranchResolvesToUnknown() {
    assertThat(resolve(unknownType(), intrinsic(IntrinsicType.INTEGER))).matches(Type::isUnknown);
    assertThat(resolve(intrinsic(IntrinsicType.INTEGER), unknownType())).matches(Type::isUnknown);
  }

  @Test
  void testDescendantAndAncestorResolveToTheAncestor() {
    StructType object = struct("TObject", CLASS);
    StructType ancestor = struct("TAncestor", CLASS, object);
    StructType descendant = struct("TDescendant", CLASS, ancestor);

    assertThat(resolve(descendant, ancestor)).isEqualTo(ancestor);
    assertThat(resolve(ancestor, descendant)).isEqualTo(ancestor);
  }

  @Test
  void testClassesWithNoCommonAncestorResolveToUnknown() {
    assertThat(resolve(struct("TFoo", CLASS), struct("TBar", CLASS))).matches(Type::isUnknown);
  }

  @Test
  void testSiblingsResolveToTheirCommonAncestor() {
    StructType object = struct("TObject", CLASS);
    StructType ancestor = struct("TAncestor", CLASS, object);
    StructType left = struct("TLeft", CLASS, ancestor);
    StructType right = struct("TRight", CLASS, ancestor);

    assertThat(resolve(left, right)).isEqualTo(ancestor);
    assertThat(resolve(right, left)).isEqualTo(ancestor);
  }

  @Test
  void testDistantSiblingsResolveToTheirNearestCommonAncestor() {
    StructType object = struct("TObject", CLASS);
    StructType ancestor = struct("TAncestor", CLASS, object);
    StructType middle = struct("TMiddle", CLASS, ancestor);
    StructType left = struct("TLeft", CLASS, middle);
    StructType right = struct("TRight", CLASS, ancestor);

    assertThat(resolve(left, right)).isEqualTo(ancestor);
    assertThat(resolve(right, left)).isEqualTo(ancestor);
    assertThat(resolve(left, middle)).isEqualTo(middle);
    assertThat(resolve(middle, left)).isEqualTo(middle);
  }

  @Test
  void testInterfacesResolveToTheirCommonAncestor() {
    StructType iinterface = struct("IInterface", INTERFACE);
    StructType ancestor = struct("IAncestor", INTERFACE, iinterface);
    StructType left = struct("ILeft", INTERFACE, ancestor);
    StructType right = struct("IRight", INTERFACE, ancestor);

    assertThat(resolve(left, right)).isEqualTo(ancestor);
    assertThat(resolve(right, left)).isEqualTo(ancestor);
  }

  @Test
  void testNilBranchResolvesToTheOtherReferenceType() {
    StructType object = struct("TObject", CLASS);
    StructType foo = struct("TFoo", CLASS, object);
    StructType intf = struct("IFoo", INTERFACE, struct("IInterface", INTERFACE));
    Type classReference = FACTORY.classOf("TFooClass", foo);
    Type procedural = procedure(List.of(), TypeFactory.voidType());
    Type dynamicArray =
        FACTORY.array(null, intrinsic(IntrinsicType.INTEGER), Set.of(ArrayOption.DYNAMIC));

    assertThat(resolve(foo, nilPointer())).isEqualTo(foo);
    assertThat(resolve(nilPointer(), foo)).isEqualTo(foo);
    assertThat(resolve(intf, nilPointer())).isEqualTo(intf);
    assertThat(resolve(nilPointer(), intf)).isEqualTo(intf);
    assertThat(resolve(classReference, nilPointer())).isEqualTo(classReference);
    assertThat(resolve(nilPointer(), classReference)).isEqualTo(classReference);
    assertThat(resolve(procedural, nilPointer())).isEqualTo(procedural);
    assertThat(resolve(nilPointer(), procedural)).isEqualTo(procedural);
    assertThat(resolve(dynamicArray, nilPointer())).isEqualTo(dynamicArray);
    assertThat(resolve(nilPointer(), dynamicArray)).isEqualTo(dynamicArray);
  }

  @Test
  void testDistinctObjectBranchesResolveToUnknown() {
    StructType ancestor = struct("TObjAncestor", StructKind.OBJECT);
    StructType left = struct("TObjLeft", StructKind.OBJECT, ancestor);
    StructType right = struct("TObjRight", StructKind.OBJECT, ancestor);
    assertThat(resolve(left, right)).matches(Type::isUnknown);
  }

  @Test
  void testDistinctRecordBranchesResolveToUnknown() {
    StructType left = struct("TRecLeft", StructKind.RECORD);
    StructType right = struct("TRecRight", StructKind.RECORD);
    assertThat(resolve(left, right)).matches(Type::isUnknown);
  }

  @Test
  void testArrayBranchesResolveOnlyAsTheSameType() {
    Type element = intrinsic(IntrinsicType.INTEGER);
    Type fixedArray = FACTORY.array("TArrA", element, Set.of(ArrayOption.FIXED));

    assertThat(resolve(fixedArray, fixedArray)).isEqualTo(fixedArray);
    assertThat(resolve(fixedArray, FACTORY.array("TArrB", element, Set.of(ArrayOption.FIXED))))
        .matches(Type::isUnknown);
    assertThat(
            resolve(
                FACTORY.array("TDynA", element, Set.of(ArrayOption.DYNAMIC)),
                FACTORY.array("TDynB", element, Set.of(ArrayOption.DYNAMIC))))
        .matches(Type::isUnknown);
  }

  @Test
  void testDistinctEnumBranchesResolveToUnknown() {
    Type enumA = FACTORY.enumeration("TEnumA", DelphiScope.unknownScope());
    Type enumB = FACTORY.enumeration("TEnumB", DelphiScope.unknownScope());

    assertThat(resolve(enumA, enumB)).matches(Type::isUnknown);
    assertThat(resolve(enumB, enumA)).matches(Type::isUnknown);
  }

  @Test
  void testSetWithContainingElementRangeWins() {
    Type small = FACTORY.set(subrange(0, 7));
    Type wide = FACTORY.set(subrange(0, 100));
    Type byteSet = FACTORY.set(intrinsic(IntrinsicType.BYTE));

    assertThat(resolve(small, wide)).isSameAs(wide);
    assertThat(resolve(wide, small)).isSameAs(wide);
    assertThat(resolve(small, byteSet)).isSameAs(byteSet);
    assertThat(resolve(byteSet, small)).isSameAs(byteSet);
  }

  @Test
  void testCharSubrangeSetsResolveToTheFirstOperand() {
    Type lower = FACTORY.set(FACTORY.subrange("'a'..'z'", intrinsic(IntrinsicType.ANSICHAR)));
    Type digits = FACTORY.set(FACTORY.subrange("'0'..'9'", intrinsic(IntrinsicType.ANSICHAR)));

    assertThat(resolve(lower, digits)).isSameAs(lower);
    assertThat(resolve(digits, lower)).isSameAs(digits);
  }

  @Test
  void testArrayConstructorTakesTheOtherSetOperandsType() {
    Type enumeration = FACTORY.enumeration("TEnum", DelphiScope.unknownScope());
    Type set = FACTORY.set(enumeration);
    Type constructor = FACTORY.arrayConstructor(List.of(enumeration));
    Type empty = FACTORY.arrayConstructor(List.of());

    assertThat(resolve(set, constructor)).isEqualTo(set);
    assertThat(resolve(constructor, set)).isEqualTo(set);
    assertThat(resolve(set, empty)).isEqualTo(set);
    assertThat(resolve(empty, set)).isEqualTo(set);
  }

  @Test
  void testDynamicArrayAndSetConstructorResolveToUnknown() {
    Type dynamicArray =
        FACTORY.array(null, intrinsic(IntrinsicType.INTEGER), Set.of(ArrayOption.DYNAMIC));
    Type constructor = FACTORY.arrayConstructor(List.of(intrinsic(IntrinsicType.INTEGER)));
    Type empty = FACTORY.arrayConstructor(List.of());

    assertThat(resolve(dynamicArray, constructor)).matches(Type::isUnknown);
    assertThat(resolve(constructor, dynamicArray)).matches(Type::isUnknown);
    assertThat(resolve(dynamicArray, empty)).matches(Type::isUnknown);
    assertThat(resolve(empty, dynamicArray)).matches(Type::isUnknown);
  }

  @Test
  void testDynamicArrayAndSetResolveToUnknown() {
    Type dynamicArray =
        FACTORY.array(null, intrinsic(IntrinsicType.BYTE), Set.of(ArrayOption.DYNAMIC));
    Type set = FACTORY.set(intrinsic(IntrinsicType.BYTE));

    assertThat(resolve(dynamicArray, set)).matches(Type::isUnknown);
    assertThat(resolve(set, dynamicArray)).matches(Type::isUnknown);
  }

  private static Stream<Arguments> ordinalConstructorElements() {
    Type enumeration = FACTORY.enumeration("TEnum", DelphiScope.unknownScope());
    return Stream.of(
        Arguments.of(intrinsic(IntrinsicType.SHORTINT), intrinsic(IntrinsicType.BYTE)),
        Arguments.of(enumeration, FACTORY.subrange("eOne..eTwo", enumeration)),
        Arguments.of(intrinsic(IntrinsicType.ANSICHAR), intrinsic(IntrinsicType.WIDECHAR)),
        Arguments.of(intrinsic(IntrinsicType.BOOLEAN), intrinsic(IntrinsicType.BYTEBOOL)),
        Arguments.of(subrange(0, 9), subrange(0, 5)));
  }

  @ParameterizedTest
  @MethodSource("ordinalConstructorElements")
  void testOrdinalSetConstructorsResolveToTheFirstConstructor(Type thenElement, Type elseElement) {
    Type left = FACTORY.arrayConstructor(List.of(thenElement));
    Type right = FACTORY.arrayConstructor(List.of(elseElement));

    assertThat(resolve(left, right)).isSameAs(left);
    assertThat(resolve(right, left)).isSameAs(right);
  }

  @Test
  void testSetConstructorsOfNonOrdinalElementsResolveToUnknown() {
    Type strings = FACTORY.arrayConstructor(List.of(intrinsic(IntrinsicType.UNICODESTRING)));
    Type integers = FACTORY.arrayConstructor(List.of(intrinsic(IntrinsicType.INTEGER)));

    assertThat(resolve(strings, integers)).matches(Type::isUnknown);
    assertThat(resolve(integers, strings)).matches(Type::isUnknown);
  }

  /** `nil` combined with any array constructor yields an anonymous dynamic array. */
  @Test
  void testNilBranchGivesAConstructorItsDynamicArrayReading() {
    Type ordinal = FACTORY.arrayConstructor(List.of(intrinsic(IntrinsicType.INTEGER)));
    Type strings = FACTORY.arrayConstructor(List.of(intrinsic(IntrinsicType.UNICODESTRING)));

    Type result = resolve(nilPointer(), ordinal);
    assertThat(result.isDynamicArray()).isTrue();
    assertThat(((CollectionType) result).elementType()).isEqualTo(intrinsic(IntrinsicType.INTEGER));

    result = resolve(strings, nilPointer());
    assertThat(result.isDynamicArray()).isTrue();
    assertThat(((CollectionType) result).elementType())
        .isEqualTo(intrinsic(IntrinsicType.UNICODESTRING));

    Type integers =
        FACTORY.arrayConstructor(
            List.of(intrinsic(IntrinsicType.BYTE), intrinsic(IntrinsicType.INTEGER)));
    result = resolve(nilPointer(), integers);
    assertThat(result.isDynamicArray()).isTrue();
    assertThat(((CollectionType) result).elementType()).isEqualTo(intrinsic(IntrinsicType.INTEGER));

    Type single = FACTORY.arrayConstructor(List.of(intrinsic(IntrinsicType.SINGLE)));
    result = resolve(nilPointer(), single);
    assertThat(result.isDynamicArray()).isTrue();
    assertThat(((CollectionType) result).elementType())
        .isEqualTo(intrinsic(IntrinsicType.EXTENDED));
  }

  @Test
  void testNilBranchWithConstructorOfMixedElementFamiliesResolvesToUnknown() {
    Type integerAndString =
        FACTORY.arrayConstructor(
            List.of(intrinsic(IntrinsicType.INTEGER), intrinsic(IntrinsicType.UNICODESTRING)));
    Type integerAndReal =
        FACTORY.arrayConstructor(
            List.of(intrinsic(IntrinsicType.INTEGER), intrinsic(IntrinsicType.DOUBLE)));
    Type realAndInteger =
        FACTORY.arrayConstructor(
            List.of(intrinsic(IntrinsicType.DOUBLE), intrinsic(IntrinsicType.INTEGER)));
    Type realAndString =
        FACTORY.arrayConstructor(
            List.of(intrinsic(IntrinsicType.DOUBLE), intrinsic(IntrinsicType.UNICODESTRING)));

    assertThat(resolve(nilPointer(), integerAndString)).matches(Type::isUnknown);
    assertThat(resolve(integerAndString, nilPointer())).matches(Type::isUnknown);
    assertThat(resolve(nilPointer(), integerAndReal)).matches(Type::isUnknown);
    assertThat(resolve(integerAndReal, nilPointer())).matches(Type::isUnknown);
    assertThat(resolve(nilPointer(), realAndInteger)).matches(Type::isUnknown);
    assertThat(resolve(nilPointer(), realAndString)).matches(Type::isUnknown);
  }

  @Test
  void testNilBranchWithEmptyConstructorResolvesToUnknown() {
    Type empty = FACTORY.arrayConstructor(List.of());

    assertThat(resolve(nilPointer(), empty)).matches(Type::isUnknown);
    assertThat(resolve(empty, nilPointer())).matches(Type::isUnknown);
  }

  @Test
  void testSetConstructorOfIncompatibleElementsResolvesToUnknown() {
    Type set = FACTORY.set(FACTORY.enumeration("TEnumA", DelphiScope.unknownScope()));
    Type constructor =
        FACTORY.arrayConstructor(
            List.of(FACTORY.enumeration("TEnumB", DelphiScope.unknownScope())));

    assertThat(resolve(set, constructor)).matches(Type::isUnknown);
    assertThat(resolve(constructor, set)).matches(Type::isUnknown);
  }

  @Test
  void testSetBranchesResolveOnlyByMatchingElementType() {
    Type enumA = FACTORY.enumeration("TEnumA", DelphiScope.unknownScope());
    Type enumB = FACTORY.enumeration("TEnumB", DelphiScope.unknownScope());
    Type setA = FACTORY.set(enumA);

    assertThat(resolve(setA, FACTORY.set(enumA))).isEqualTo(setA);
    assertThat(resolve(setA, FACTORY.set(enumB))).matches(Type::isUnknown);
  }

  @Test
  void testOverlappingIntegerSetsResolveToTheLargeSetOfByte() {
    Type left = FACTORY.set(subrange(0, 10));
    Type right = FACTORY.set(subrange(5, 20));

    Type result = resolve(left, right);
    assertThat(result.isSet()).isTrue();
    assertThat(((CollectionType) result).elementType()).isEqualTo(intrinsic(IntrinsicType.BYTE));

    result = resolve(right, left);
    assertThat(result.isSet()).isTrue();
    assertThat(((CollectionType) result).elementType()).isEqualTo(intrinsic(IntrinsicType.BYTE));
  }

  @Test
  void testDistinctCharSetsResolveToTheLargeSetOfAnsiChar() {
    Type ansi = FACTORY.set(intrinsic(IntrinsicType.ANSICHAR));
    Type wide = FACTORY.set(intrinsic(IntrinsicType.WIDECHAR));

    Type result = resolve(ansi, wide);
    assertThat(result.isSet()).isTrue();
    assertThat(((CollectionType) result).elementType())
        .isEqualTo(intrinsic(IntrinsicType.ANSICHAR));

    result = resolve(wide, ansi);
    assertThat(result.isSet()).isTrue();
    assertThat(((CollectionType) result).elementType())
        .isEqualTo(intrinsic(IntrinsicType.ANSICHAR));
  }

  @Test
  void testIntegerAndCharSetsResolveToUnknown() {
    Type integers = FACTORY.set(subrange(0, 10));
    Type chars = FACTORY.set(intrinsic(IntrinsicType.ANSICHAR));

    assertThat(resolve(integers, chars)).matches(Type::isUnknown);
    assertThat(resolve(chars, integers)).matches(Type::isUnknown);
  }

  @Test
  void testProceduralBranchesWithDifferentSignaturesResolveToUnknown() {
    Type noArguments = procedure(List.of(), TypeFactory.voidType());
    Type oneArgument = procedure(List.of(intrinsic(IntrinsicType.INTEGER)), TypeFactory.voidType());
    Type stringArgument =
        procedure(List.of(intrinsic(IntrinsicType.UNICODESTRING)), TypeFactory.voidType());
    Type integerFunction = procedure(List.of(), intrinsic(IntrinsicType.INTEGER));
    Type booleanFunction = procedure(List.of(), intrinsic(IntrinsicType.BOOLEAN));

    assertThat(resolve(noArguments, oneArgument)).matches(Type::isUnknown);
    assertThat(resolve(oneArgument, noArguments)).matches(Type::isUnknown);
    assertThat(resolve(oneArgument, stringArgument)).matches(Type::isUnknown);
    assertThat(resolve(stringArgument, oneArgument)).matches(Type::isUnknown);
    assertThat(resolve(noArguments, integerFunction)).matches(Type::isUnknown);
    assertThat(resolve(integerFunction, noArguments)).matches(Type::isUnknown);
    assertThat(resolve(integerFunction, booleanFunction)).matches(Type::isUnknown);
    assertThat(resolve(booleanFunction, integerFunction)).matches(Type::isUnknown);
  }

  @ParameterizedTest
  @CsvSource({"PROCEDURE", "PROCEDURE_OF_OBJECT", "REFERENCE", "ANONYMOUS"})
  void testSameSignatureProceduralBranchesResolveToTheFirstOperand(ProceduralKind kind) {
    List<Type> parameters = List.of(intrinsic(IntrinsicType.INTEGER));
    Type left = procedural(kind, parameters, TypeFactory.voidType());
    Type right = procedural(kind, parameters, TypeFactory.voidType());

    assertThat(resolve(left, right)).isSameAs(left);
    assertThat(resolve(right, left)).isSameAs(right);
  }

  /** A named method reference absorbs an anonymous method with the same signature. */
  @Test
  void testMethodReferenceAbsorbsAnonymousMethodOfTheSameSignature() {
    List<Type> parameters = List.of(intrinsic(IntrinsicType.INTEGER));
    Type reference = procedural(ProceduralKind.REFERENCE, parameters, TypeFactory.voidType());
    Type anonymous = procedural(ProceduralKind.ANONYMOUS, parameters, TypeFactory.voidType());

    assertThat(resolve(reference, anonymous)).isSameAs(reference);
    assertThat(resolve(anonymous, reference)).isSameAs(reference);
  }

  @Test
  void testMethodReferenceAndAnonymousMethodOfDifferentSignaturesResolveToUnknown() {
    Type reference =
        procedural(
            ProceduralKind.REFERENCE,
            List.of(intrinsic(IntrinsicType.INTEGER)),
            TypeFactory.voidType());
    Type anonymous = procedural(ProceduralKind.ANONYMOUS, List.of(), TypeFactory.voidType());

    assertThat(resolve(reference, anonymous)).matches(Type::isUnknown);
    assertThat(resolve(anonymous, reference)).matches(Type::isUnknown);
  }

  /** Mixed procedural kinds are a compile error even with matching signatures. */
  @Test
  void testMixedProceduralKindBranchesResolveToUnknown() {
    List<Type> parameters = List.of(intrinsic(IntrinsicType.INTEGER));
    Type plain = procedural(ProceduralKind.PROCEDURE, parameters, TypeFactory.voidType());
    Type method =
        procedural(ProceduralKind.PROCEDURE_OF_OBJECT, parameters, TypeFactory.voidType());
    Type reference = procedural(ProceduralKind.REFERENCE, parameters, TypeFactory.voidType());
    Type anonymous = procedural(ProceduralKind.ANONYMOUS, parameters, TypeFactory.voidType());

    assertThat(resolve(plain, method)).matches(Type::isUnknown);
    assertThat(resolve(method, plain)).matches(Type::isUnknown);
    assertThat(resolve(reference, plain)).matches(Type::isUnknown);
    assertThat(resolve(plain, reference)).matches(Type::isUnknown);
    assertThat(resolve(anonymous, plain)).matches(Type::isUnknown);
    assertThat(resolve(plain, anonymous)).matches(Type::isUnknown);
  }

  @Test
  void testNilBranchResolvesToTheOtherMethodReference() {
    Type reference = procedural(ProceduralKind.REFERENCE, List.of(), TypeFactory.voidType());

    assertThat(resolve(reference, nilPointer())).isEqualTo(reference);
    assertThat(resolve(nilPointer(), reference)).isEqualTo(reference);
  }

  @Test
  void testDistinctFileBranchesResolveToUnknown() {
    assertThat(
            resolve(
                FACTORY.fileOf(intrinsic(IntrinsicType.INTEGER)),
                FACTORY.fileOf(intrinsic(IntrinsicType.UNICODESTRING))))
        .matches(Type::isUnknown);
  }

  /** `nil` is only assignable to reference types; a value type alongside it is a compile error. */
  @ParameterizedTest
  @CsvSource({
    "INTEGER",
    "ANSICHAR",
    "WIDECHAR",
    "UNICODESTRING",
    "ANSISTRING",
    "WIDESTRING",
    "SHORTSTRING",
    "BOOLEAN",
    "DOUBLE",
    "VARIANT"
  })
  void testNilAndValueTypeBranchesResolveToUnknown(IntrinsicType value) {
    assertThat(resolve(nilPointer(), intrinsic(value))).matches(Type::isUnknown);
    assertThat(resolve(intrinsic(value), nilPointer())).matches(Type::isUnknown);
  }

  @Test
  void testNilAndNonReferenceStructuralBranchesResolveToUnknown() {
    Type enumeration = FACTORY.enumeration("TEnum", DelphiScope.unknownScope());
    Type set = FACTORY.set(enumeration);
    Type fixedArray =
        FACTORY.array(null, intrinsic(IntrinsicType.INTEGER), Set.of(ArrayOption.FIXED));

    assertThat(resolve(nilPointer(), enumeration)).matches(Type::isUnknown);
    assertThat(resolve(enumeration, nilPointer())).matches(Type::isUnknown);
    assertThat(resolve(nilPointer(), set)).matches(Type::isUnknown);
    assertThat(resolve(nilPointer(), fixedArray)).matches(Type::isUnknown);
  }

  /**
   * The common ancestor of two classes is a class: an interface both happen to implement never
   * wins, however near it is.
   */
  @Test
  void testClassesSharingAnInterfaceResolveToTheirCommonClassAncestor() {
    StructType object = struct("TObject", CLASS);
    StructType iinterface = struct("IInterface", INTERFACE);
    StructType shared = struct("IShared", INTERFACE, iinterface);
    StructType base = struct("TBase", CLASS, object);
    StructType foo = struct("TFoo", CLASS, shared, base);
    StructType bar = struct("TBar", CLASS, shared, base);

    assertThat(resolve(foo, bar)).isEqualTo(base);
    assertThat(resolve(bar, foo)).isEqualTo(base);
  }

  @Test
  void testClassAndInterfaceBranchesResolveToUnknown() {
    StructType object = struct("TObject", CLASS);
    StructType iinterface = struct("IInterface", INTERFACE);
    StructType intf = struct("IFoo", INTERFACE, iinterface);
    StructType clazz = struct("TFoo", CLASS, intf, object);

    assertThat(resolve(clazz, intf)).matches(Type::isUnknown);
    assertThat(resolve(intf, clazz)).matches(Type::isUnknown);
  }

  @Test
  void testPointerBranchesCollapseToPointerUnlessTheSecondOperandIsUntypedOrNil() {
    Type pointer = intrinsic(IntrinsicType.POINTER);
    Type pAnsiChar = intrinsic(IntrinsicType.PANSICHAR);
    Type pWideChar = intrinsic(IntrinsicType.PWIDECHAR);

    assertThat(resolve(pAnsiChar, pWideChar)).isEqualTo(pointer);
    assertThat(resolve(pWideChar, pAnsiChar)).isEqualTo(pointer);
    assertThat(resolve(pAnsiChar, pointer)).isEqualTo(pAnsiChar);
    assertThat(resolve(pointer, pAnsiChar)).isEqualTo(pointer);
    assertThat(resolve(pAnsiChar, nilPointer())).isEqualTo(pAnsiChar);
    assertThat(resolve(nilPointer(), pAnsiChar)).isEqualTo(pointer);
  }

  @ParameterizedTest
  @CsvSource({"ANSICHAR", "WIDECHAR", "SHORTSTRING", "ANSISTRING", "WIDESTRING", "UNICODESTRING"})
  void testPWideCharAndTextualBranchesResolveToUnicodeString(IntrinsicType textual) {
    Type expected = intrinsic(IntrinsicType.UNICODESTRING);

    assertThat(resolve(intrinsic(IntrinsicType.PWIDECHAR), intrinsic(textual))).isEqualTo(expected);
    assertThat(resolve(intrinsic(textual), intrinsic(IntrinsicType.PWIDECHAR))).isEqualTo(expected);
    assertThat(resolve(intrinsic(IntrinsicType.PANSICHAR), intrinsic(textual)))
        .matches(Type::isUnknown);
    assertThat(resolve(intrinsic(textual), intrinsic(IntrinsicType.PANSICHAR)))
        .matches(Type::isUnknown);
  }

  @Test
  void testClassReferenceBranchesResolveToAReferenceToTheCommonAncestor() {
    StructType object = struct("TObject", CLASS);
    StructType ancestor = struct("TAncestor", CLASS, object);
    StructType middle = struct("TMiddle", CLASS, ancestor);

    Type ancestorRef = FACTORY.classOf("TClassRef", ancestor);
    Type middleRef = FACTORY.classOf("TClassRefMid", middle);

    assertThat(resolve(middleRef, ancestorRef)).isEqualTo(ancestorRef);
    assertThat(resolve(ancestorRef, middleRef)).isEqualTo(ancestorRef);
  }

  @Test
  void testSiblingClassReferenceBranchesResolveToAReferenceToTheCommonAncestor() {
    StructType object = struct("TObject", CLASS);
    StructType ancestor = struct("TAncestor", CLASS, object);
    StructType left = struct("TLeft", CLASS, ancestor);
    StructType right = struct("TRight", CLASS, ancestor);

    Type leftRef = FACTORY.classOf("TLeftClass", left);
    Type rightRef = FACTORY.classOf("TRightClass", right);

    Type result = resolve(leftRef, rightRef);
    assertThat(result).isInstanceOf(ClassReferenceType.class);
    assertThat(((ClassReferenceType) result).classType()).isEqualTo(ancestor);
  }

  @Test
  void testClassReferencesWithNoCommonAncestorResolveToUnknown() {
    Type left = FACTORY.classOf("TFooClass", struct("TFoo", CLASS));
    Type right = FACTORY.classOf("TBarClass", struct("TBar", CLASS));

    assertThat(resolve(left, right)).matches(Type::isUnknown);
    assertThat(resolve(right, left)).matches(Type::isUnknown);
  }

  @Test
  void testEnumAndSubrangeOfThatEnumResolveToTheEnum() {
    Type enumeration = FACTORY.enumeration("TEnum", DelphiScope.unknownScope());
    Type subrange = FACTORY.subrange("eOne..eTwo", enumeration);

    assertThat(resolve(enumeration, subrange)).isEqualTo(enumeration);
    assertThat(resolve(subrange, enumeration)).isEqualTo(enumeration);
  }

  @Test
  void testStrongAliasOfStringKeepsItsDeclaredTypeOnTies() {
    Type myString = strongAlias("MyString", intrinsic(IntrinsicType.UNICODESTRING));
    Type myString2 = strongAlias("MyString2", intrinsic(IntrinsicType.UNICODESTRING));

    assertThat(resolve(myString, intrinsic(IntrinsicType.UNICODESTRING))).isEqualTo(myString);
    assertThat(resolve(intrinsic(IntrinsicType.UNICODESTRING), myString))
        .isEqualTo(intrinsic(IntrinsicType.UNICODESTRING));
    assertThat(resolve(myString, myString2)).isEqualTo(myString);
    assertThat(resolve(myString2, myString)).isEqualTo(myString2);
  }

  @Test
  void testWinningStringOperandKeepsItsStrongAliasType() {
    Type myString = strongAlias("MyString", intrinsic(IntrinsicType.UNICODESTRING));

    assertThat(resolve(myString, intrinsic(IntrinsicType.ANSISTRING))).isEqualTo(myString);
    assertThat(resolve(intrinsic(IntrinsicType.ANSISTRING), myString)).isEqualTo(myString);
    assertThat(resolve(myString, intrinsic(IntrinsicType.WIDESTRING))).isEqualTo(myString);
  }

  @Test
  void testStrongAliasOfBooleanKeepsItsDeclaredTypeOnTies() {
    Type myBool = strongAlias("MyBool", intrinsic(IntrinsicType.BOOLEAN));

    assertThat(resolve(myBool, intrinsic(IntrinsicType.BOOLEAN))).isEqualTo(myBool);
    assertThat(resolve(intrinsic(IntrinsicType.BOOLEAN), myBool))
        .isEqualTo(intrinsic(IntrinsicType.BOOLEAN));
    assertThat(resolve(myBool, intrinsic(IntrinsicType.BYTEBOOL))).isEqualTo(myBool);
  }

  @Test
  void testStrongAliasOfIntegerCollapsesToTheCommonIntegerType() {
    Type myInt = strongAlias("MyInt", intrinsic(IntrinsicType.INTEGER));
    Type myInt2 = strongAlias("MyInt2", intrinsic(IntrinsicType.INTEGER));

    assertThat(resolve(myInt, intrinsic(IntrinsicType.INTEGER)))
        .isEqualTo(intrinsic(IntrinsicType.INTEGER));
    assertThat(resolve(myInt, myInt2)).isEqualTo(intrinsic(IntrinsicType.INTEGER));
    assertThat(resolve(myInt, intrinsic(IntrinsicType.BYTE)))
        .isEqualTo(intrinsic(IntrinsicType.INTEGER));
    assertThat(resolve(myInt, intrinsic(IntrinsicType.INT64)))
        .isEqualTo(intrinsic(IntrinsicType.INT64));
  }

  @Test
  void testStrongAliasOfPointerCollapsesToTheUntypedPointer() {
    Type pInt = FACTORY.pointerTo("PInt", intrinsic(IntrinsicType.INTEGER));
    Type myPInt = strongAlias("MyPInt", pInt);
    Type myPInt2 = strongAlias("MyPInt2", pInt);

    assertThat(resolve(myPInt, pInt)).isEqualTo(intrinsic(IntrinsicType.POINTER));
    assertThat(resolve(pInt, myPInt)).isEqualTo(intrinsic(IntrinsicType.POINTER));
    assertThat(resolve(myPInt, myPInt2)).isEqualTo(intrinsic(IntrinsicType.POINTER));
  }

  @Test
  void testStrongAliasOfTheUntypedPointerKeepsItsDeclaredTypeAsFirstOperand() {
    Type myPointer = strongAlias("MyPointer", intrinsic(IntrinsicType.POINTER));

    assertThat(resolve(myPointer, intrinsic(IntrinsicType.POINTER))).isEqualTo(myPointer);
    assertThat(resolve(intrinsic(IntrinsicType.POINTER), myPointer))
        .isEqualTo(intrinsic(IntrinsicType.POINTER));
  }

  @Test
  void testStrongAliasOfSetKeepsItsDeclaredTypeOnTies() {
    Type enumeration = FACTORY.enumeration("TEnum", DelphiScope.unknownScope());
    Type set = FACTORY.set(enumeration);
    Type mySet = strongAlias("MySet", set);
    Type mySet2 = strongAlias("MySet2", set);

    assertThat(resolve(mySet, set)).isEqualTo(mySet);
    assertThat(resolve(set, mySet)).isEqualTo(set);
    assertThat(resolve(mySet, mySet2)).isEqualTo(mySet);
  }

  @Test
  void testStrongAliasOfEnumCollapsesToTheEnum() {
    Type enumeration = FACTORY.enumeration("TEnum", DelphiScope.unknownScope());
    Type myEnum = strongAlias("MyEnum", enumeration);

    assertThat(resolve(myEnum, enumeration)).isEqualTo(enumeration);
    assertThat(resolve(enumeration, myEnum)).isEqualTo(enumeration);
  }

  @Test
  void testStrongAliasOfShortStringKeepsItsDeclaredTypeOnlyWhenItWins() {
    Type myShort = strongAlias("MyShort", intrinsic(IntrinsicType.SHORTSTRING));

    assertThat(resolve(myShort, intrinsic(IntrinsicType.SHORTSTRING))).isEqualTo(myShort);
    assertThat(resolve(intrinsic(IntrinsicType.SHORTSTRING), myShort))
        .isEqualTo(intrinsic(IntrinsicType.SHORTSTRING));
    assertThat(resolve(myShort, intrinsic(IntrinsicType.ANSICHAR))).isEqualTo(myShort);
    assertThat(resolve(myShort, intrinsic(IntrinsicType.UNICODESTRING)))
        .isEqualTo(intrinsic(IntrinsicType.UNICODESTRING));
  }

  @Test
  void testStrongAliasOfAnsiAndWideStringKeepsItsDeclaredTypeOnlyWhenItWins() {
    Type myAnsi = strongAlias("MyAnsi", intrinsic(IntrinsicType.ANSISTRING));
    Type myWide = strongAlias("MyWide", intrinsic(IntrinsicType.WIDESTRING));

    assertThat(resolve(myAnsi, intrinsic(IntrinsicType.ANSISTRING))).isEqualTo(myAnsi);
    assertThat(resolve(myAnsi, intrinsic(IntrinsicType.WIDESTRING)))
        .isEqualTo(intrinsic(IntrinsicType.WIDESTRING));
    assertThat(resolve(myWide, intrinsic(IntrinsicType.WIDESTRING))).isEqualTo(myWide);
    assertThat(resolve(myWide, intrinsic(IntrinsicType.UNICODESTRING)))
        .isEqualTo(intrinsic(IntrinsicType.UNICODESTRING));
  }

  @Test
  void testStrongAliasOfCurrencyAbsorbsIntegerWhereExtendedCannotHoldIt() {
    Type currency = DCC64_FACTORY.getIntrinsic(IntrinsicType.CURRENCY);
    Type myCurrency = DCC64_FACTORY.strongAlias("MyCurrency", currency);

    assertThat(DCC64_RESOLVER.commonType(myCurrency, currency)).isEqualTo(currency);
    assertThat(
            DCC64_RESOLVER.commonType(
                myCurrency, DCC64_FACTORY.getIntrinsic(IntrinsicType.INTEGER)))
        .isEqualTo(currency);
  }

  @Test
  void testStrongAliasOfPWideCharCombinesWithText() {
    Type myPWideChar = strongAlias("MyPWideChar", intrinsic(IntrinsicType.PWIDECHAR));

    assertThat(resolve(myPWideChar, intrinsic(IntrinsicType.UNICODESTRING)))
        .isEqualTo(intrinsic(IntrinsicType.UNICODESTRING));
    assertThat(resolve(myPWideChar, intrinsic(IntrinsicType.PWIDECHAR)))
        .isEqualTo(intrinsic(IntrinsicType.POINTER));
  }

  @Test
  void testStrongAliasOfCharCollapsesToTheChar() {
    Type myChar = strongAlias("MyChar", intrinsic(IntrinsicType.WIDECHAR));

    assertThat(resolve(myChar, intrinsic(IntrinsicType.WIDECHAR)))
        .isEqualTo(intrinsic(IntrinsicType.WIDECHAR));
    assertThat(resolve(intrinsic(IntrinsicType.WIDECHAR), myChar))
        .isEqualTo(intrinsic(IntrinsicType.WIDECHAR));
  }

  @Test
  void testStrongAliasOfRealWidensToExtended() {
    Type myDouble = strongAlias("MyDouble", intrinsic(IntrinsicType.DOUBLE));

    assertThat(resolve(myDouble, intrinsic(IntrinsicType.DOUBLE)))
        .isEqualTo(intrinsic(IntrinsicType.EXTENDED));
    assertThat(resolve(myDouble, intrinsic(IntrinsicType.SINGLE)))
        .isEqualTo(intrinsic(IntrinsicType.EXTENDED));
  }

  @Test
  void testCharAndSubrangeOfThatCharResolveToTheChar() {
    Type wideChar = intrinsic(IntrinsicType.WIDECHAR);
    Type subrange = FACTORY.subrange("'0'..'9'", wideChar);

    assertThat(resolve(wideChar, subrange)).isEqualTo(wideChar);
    assertThat(resolve(subrange, wideChar)).isEqualTo(wideChar);
  }

  @Test
  void testCharSubrangeCombinesWithTextualTypesThroughItsHost() {
    Type digit = FACTORY.subrange("'0'..'9'", intrinsic(IntrinsicType.WIDECHAR));
    Type unicodeString = intrinsic(IntrinsicType.UNICODESTRING);
    Type wideString = intrinsic(IntrinsicType.WIDESTRING);

    assertThat(resolve(intrinsic(IntrinsicType.ANSICHAR), digit)).isEqualTo(unicodeString);
    assertThat(resolve(digit, intrinsic(IntrinsicType.ANSICHAR))).isEqualTo(unicodeString);
    assertThat(resolve(digit, unicodeString)).isEqualTo(unicodeString);
    assertThat(resolve(digit, intrinsic(IntrinsicType.ANSISTRING))).isEqualTo(unicodeString);
    assertThat(resolve(digit, wideString)).isEqualTo(wideString);
    assertThat(resolve(digit, intrinsic(IntrinsicType.PWIDECHAR))).isEqualTo(unicodeString);
    assertThat(resolve(digit, intrinsic(IntrinsicType.SHORTSTRING))).matches(Type::isUnknown);
  }

  @Test
  void testBooleanSubrangeBranchesResolveToTheFirstOperand() {
    Type boolSubrange = FACTORY.subrange("False..True", intrinsic(IntrinsicType.BOOLEAN));

    assertThat(resolve(intrinsic(IntrinsicType.BOOLEAN), boolSubrange))
        .isEqualTo(intrinsic(IntrinsicType.BOOLEAN));
    assertThat(resolve(boolSubrange, intrinsic(IntrinsicType.BOOLEAN))).isEqualTo(boolSubrange);
    assertThat(resolve(intrinsic(IntrinsicType.BYTEBOOL), boolSubrange))
        .isEqualTo(intrinsic(IntrinsicType.BYTEBOOL));
    assertThat(resolve(boolSubrange, intrinsic(IntrinsicType.BYTEBOOL))).isEqualTo(boolSubrange);
  }

  @Test
  void testStrongAliasOfVariantResolvesToVariant() {
    Type myVariant = strongAlias("MyVariant", intrinsic(IntrinsicType.VARIANT));

    assertThat(resolve(myVariant, intrinsic(IntrinsicType.VARIANT)))
        .isEqualTo(intrinsic(IntrinsicType.VARIANT));
    assertThat(resolve(intrinsic(IntrinsicType.VARIANT), myVariant))
        .isEqualTo(intrinsic(IntrinsicType.VARIANT));
    assertThat(resolve(myVariant, intrinsic(IntrinsicType.OLEVARIANT)))
        .isEqualTo(intrinsic(IntrinsicType.VARIANT));
  }

  @Test
  void testDisjointSubrangesOfOneEnumResolveToTheEnum() {
    Type enumeration = FACTORY.enumeration("TEnum", DelphiScope.unknownScope());
    Type low = FACTORY.subrange("eaOne..eaTwo", enumeration);
    Type high = FACTORY.subrange("eaThree..eaFour", enumeration);

    assertThat(resolve(low, high)).isEqualTo(enumeration);
    assertThat(resolve(high, low)).isEqualTo(enumeration);
  }

  /** A strong alias of a class is a sibling of its base: they unify at the base's parent. */
  @Test
  void testStrongAliasOfClassResolvesToTheBaseClassParent() {
    StructType object = struct("TObject", CLASS);
    StructType base = struct("TBase", CLASS, object);
    StructType derived = struct("TDerived", CLASS, base);
    Type alias = strongAlias("TMyBase", base);

    assertThat(resolve(alias, base)).isEqualTo(object);
    assertThat(resolve(base, alias)).isEqualTo(object);
    assertThat(resolve(alias, derived)).isEqualTo(object);
    assertThat(resolve(derived, alias)).isEqualTo(object);
    assertThat(resolve(alias, strongAlias("TMyBase2", base))).isEqualTo(object);
  }

  @Test
  void testStrongAliasOfInterfaceResolvesToTheBaseInterfaceParent() {
    StructType iinterface = struct("IInterface", INTERFACE);
    StructType base = struct("IFoo", INTERFACE, iinterface);
    Type alias = strongAlias("IMyFoo", base);

    assertThat(resolve(alias, base)).isEqualTo(iinterface);
    assertThat(resolve(base, alias)).isEqualTo(iinterface);
  }

  /**
   * Class references over one class with distinct identities resolve to the second (!!!) operand.
   * This is the only case where the second operand breaks the tie.
   */
  @Test
  void testReferencesToOneClassResolveToTheSecondOperand() {
    StructType object = struct("TObject", CLASS);
    StructType base = struct("TBase", CLASS, object);
    Type reference = FACTORY.classOf("TBaseClass", base);
    Type alias = strongAlias("TMyBaseClass", reference);

    assertThat(resolve(alias, reference)).isSameAs(reference);
    assertThat(resolve(reference, alias)).isSameAs(alias);
  }

  @Test
  void testPointersWithoutDeclaredNamesCollapseToTheFirstOperand() {
    Type left = FACTORY.pointerTo(null, intrinsic(IntrinsicType.INTEGER));
    Type right = FACTORY.pointerTo(null, intrinsic(IntrinsicType.INTEGER));

    assertThat(resolve(left, right)).isSameAs(left);
  }

  @Test
  void testSetKeepsItsTypeAgainstConstructorOfCompatibleElements() {
    Type set = FACTORY.set(subrange(0, 200));
    Type constructor = FACTORY.arrayConstructor(List.of(intrinsic(IntrinsicType.SHORTINT)));

    assertThat(resolve(set, constructor)).isSameAs(set);
    assertThat(resolve(constructor, set)).isSameAs(set);
  }

  private static void assertResolvesBothWays(
      IntrinsicType then, IntrinsicType els, IntrinsicType expected) {
    assertResolvesBothWays(RESOLVER, FACTORY, then, els, expected);
  }

  private static void assertResolvesBothWays(
      CommonTypeResolver resolver,
      TypeFactory factory,
      IntrinsicType then,
      IntrinsicType els,
      IntrinsicType expected) {
    assertThat(resolver.commonType(factory.getIntrinsic(then), factory.getIntrinsic(els)))
        .as("%s | %s", then, els)
        .isEqualTo(factory.getIntrinsic(expected));
    assertThat(resolver.commonType(factory.getIntrinsic(els), factory.getIntrinsic(then)))
        .as("%s | %s", els, then)
        .isEqualTo(factory.getIntrinsic(expected));
  }

  private static Type intrinsic(IntrinsicType type) {
    return FACTORY.getIntrinsic(type);
  }

  private static Type nilPointer() {
    return FACTORY.nilPointer();
  }

  private static Type strongAlias(String image, Type aliased) {
    return FACTORY.strongAlias(image, aliased);
  }

  private static Type procedure(List<Type> parameterTypes, Type returnType) {
    return procedural(ProceduralKind.PROCEDURE, parameterTypes, returnType);
  }

  private static Type procedural(ProceduralKind kind, List<Type> parameterTypes, Type returnType) {
    return FACTORY.createProcedural(
        kind,
        parameterTypes.stream().map(TypeMocker::parameter).collect(Collectors.toUnmodifiableList()),
        returnType,
        Collections.emptySet());
  }

  private static Type subrange(long min, long max) {
    return FACTORY.subrange(
        String.format("%d..%d", min, max), BigInteger.valueOf(min), BigInteger.valueOf(max));
  }

  private static Type resolve(Type first, Type second) {
    return RESOLVER.commonType(first, second);
  }
}
