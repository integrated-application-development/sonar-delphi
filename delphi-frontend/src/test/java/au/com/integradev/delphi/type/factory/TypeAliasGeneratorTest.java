/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package au.com.integradev.delphi.type.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.type.UnresolvedTypeImpl;
import au.com.integradev.delphi.type.generic.TypeParameterTypeImpl;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import au.com.integradev.delphi.utils.types.TypeMocker;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.StructKind;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.AliasType;
import org.sonar.plugins.communitydelphi.api.type.Type.AnsiStringType;
import org.sonar.plugins.communitydelphi.api.type.Type.ArrayConstructorType;
import org.sonar.plugins.communitydelphi.api.type.Type.BooleanType;
import org.sonar.plugins.communitydelphi.api.type.Type.CharacterType;
import org.sonar.plugins.communitydelphi.api.type.Type.ClassReferenceType;
import org.sonar.plugins.communitydelphi.api.type.Type.CollectionType;
import org.sonar.plugins.communitydelphi.api.type.Type.EnumType;
import org.sonar.plugins.communitydelphi.api.type.Type.FileType;
import org.sonar.plugins.communitydelphi.api.type.Type.HelperType;
import org.sonar.plugins.communitydelphi.api.type.Type.IntegerType;
import org.sonar.plugins.communitydelphi.api.type.Type.PointerType;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.Type.RealType;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;
import org.sonar.plugins.communitydelphi.api.type.Type.StringType;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;
import org.sonar.plugins.communitydelphi.api.type.Type.SubrangeType;
import org.sonar.plugins.communitydelphi.api.type.Type.TypeParameterType;
import org.sonar.plugins.communitydelphi.api.type.Type.UnknownType;
import org.sonar.plugins.communitydelphi.api.type.Type.UnresolvedType;
import org.sonar.plugins.communitydelphi.api.type.Type.VariantType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;
import org.sonar.plugins.communitydelphi.api.type.TypeSpecializationContext;

class TypeAliasGeneratorTest {
  private static final String ALIAS_NAME = "TAlias";
  private static final String ALIASED_NAME = "TAliased";
  private static final TypeFactory FACTORY = TypeFactoryUtils.defaultFactory();
  private static final Type BYTE = FACTORY.getIntrinsic(IntrinsicType.BYTE);

  static class AliasedTypeProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(FACTORY.set(BYTE), CollectionType.class),
          Arguments.of(TypeMocker.struct(ALIASED_NAME, StructKind.CLASS), StructType.class),
          Arguments.of(TypeMocker.struct(ALIASED_NAME, StructKind.INTERFACE), StructType.class),
          Arguments.of(TypeMocker.struct(ALIASED_NAME, StructKind.RECORD), StructType.class),
          Arguments.of(TypeMocker.struct(ALIASED_NAME, StructKind.CLASS_HELPER), HelperType.class),
          Arguments.of(TypeMocker.struct(ALIASED_NAME, StructKind.RECORD_HELPER), HelperType.class),
          Arguments.of(TypeMocker.struct(ALIASED_NAME, StructKind.OBJECT), StructType.class),
          Arguments.of(FACTORY.untypedPointer(), PointerType.class),
          Arguments.of(FACTORY.pointerTo(ALIASED_NAME, BYTE), PointerType.class),
          Arguments.of(
              ((TypeFactoryImpl) FACTORY).procedure(List.of(TypeMocker.parameter(BYTE)), BYTE),
              ProceduralType.class),
          Arguments.of(
              ((TypeFactoryImpl) FACTORY).ofObject(List.of(TypeMocker.parameter(BYTE)), BYTE),
              ProceduralType.class),
          Arguments.of(
              ((TypeFactoryImpl) FACTORY).method(List.of(TypeMocker.parameter(BYTE)), BYTE),
              ProceduralType.class),
          Arguments.of(
              ((TypeFactoryImpl) FACTORY).anonymous(List.of(TypeMocker.parameter(BYTE)), BYTE),
              ProceduralType.class),
          Arguments.of(FACTORY.untypedFile(), FileType.class),
          Arguments.of(FACTORY.fileOf(BYTE), FileType.class),
          Arguments.of(
              ((TypeFactoryImpl) FACTORY).enumeration(ALIASED_NAME, DelphiScope.unknownScope()),
              EnumType.class),
          Arguments.of(FACTORY.subrange(ALIASED_NAME, BYTE), SubrangeType.class),
          Arguments.of(FACTORY.classOf(ALIASED_NAME, BYTE), ClassReferenceType.class),
          Arguments.of(
              TypeParameterTypeImpl.create(ALIASED_NAME, List.of(BYTE)), TypeParameterType.class),
          Arguments.of(BYTE, IntegerType.class),
          Arguments.of(FACTORY.getIntrinsic(IntrinsicType.DOUBLE), RealType.class),
          Arguments.of(FACTORY.getIntrinsic(IntrinsicType.BOOLEAN), BooleanType.class),
          Arguments.of(FACTORY.getIntrinsic(IntrinsicType.CHAR), CharacterType.class),
          Arguments.of(FACTORY.getIntrinsic(IntrinsicType.ANSISTRING), AnsiStringType.class),
          Arguments.of(FACTORY.getIntrinsic(IntrinsicType.STRING), StringType.class),
          Arguments.of(FACTORY.getIntrinsic(IntrinsicType.VARIANT), VariantType.class),
          Arguments.of(FACTORY.getIntrinsic(IntrinsicType.OLEVARIANT), VariantType.class),
          Arguments.of(TypeFactory.unknownType(), UnknownType.class),
          Arguments.of(UnresolvedTypeImpl.referenceTo(ALIASED_NAME), UnresolvedType.class));
    }
  }

  @ParameterizedTest
  @ArgumentsSource(AliasedTypeProvider.class)
  void testWeakAlias(Type aliased, Class<? extends Type> typeInterface) {
    Type alias = FACTORY.weakAlias(ALIAS_NAME, aliased);

    assertThat(alias.isAlias()).isTrue();
    assertThat(alias.isWeakAlias()).isTrue();

    assertThat(alias.is(aliased)).isTrue();
    assertThat(alias.is(ALIAS_NAME)).isTrue();
    assertThat(alias.getImage()).isEqualTo(aliased.getImage());

    assertThat(typeInterface).isAssignableFrom(alias.getClass());

    assertAliasImplementsTypeThroughDelegation(alias, aliased);
  }

  @ParameterizedTest
  @ArgumentsSource(AliasedTypeProvider.class)
  void testStrongAlias(Type aliased, Class<? extends Type> typeInterface) {
    AliasType alias = FACTORY.strongAlias(ALIAS_NAME, aliased);

    assertThat(alias.isAlias()).isTrue();
    assertThat(alias.isStrongAlias()).isTrue();

    assertThat(alias.is(aliased)).isFalse();
    assertThat(alias.is(ALIAS_NAME)).isTrue();
    assertThat(alias.getImage()).isEqualTo(alias.aliasImage());

    assertThat(typeInterface).isAssignableFrom(alias.getClass());

    assertAliasImplementsTypeThroughDelegation(alias, aliased);
  }

  @Test
  void testWeakAliasIsIntrinsicType() {
    Type alias = FACTORY.weakAlias(ALIAS_NAME, BYTE);
    assertThat(alias.is(IntrinsicType.BYTE)).isTrue();
  }

  @Test
  void testStrongAliasIsNotIntrinsicType() {
    Type alias = FACTORY.strongAlias(ALIAS_NAME, BYTE);
    assertThat(alias.is(IntrinsicType.BYTE)).isFalse();
  }

  @Test
  void testSpecializationShouldReturnSelf() {
    TypeParameterType element = TypeParameterTypeImpl.create("T");

    TypeSpecializationContext specializationContext = mock();
    when(specializationContext.getArgument(element)).thenReturn(BYTE);
    when(specializationContext.hasSignatureMismatch()).thenReturn(false);

    Type aliased =
        ((TypeFactoryImpl) FACTORY).array(ALIASED_NAME, element, Set.of(ArrayOption.DYNAMIC));
    Type alias = FACTORY.weakAlias(ALIAS_NAME, aliased);
    Type specialized = alias.specialize(specializationContext);

    assertThat(specialized).isSameAs(alias);
  }

  @Test
  void testUnhandledTypeInterfaceShouldThrow() {
    Type aliased = mock();

    assertThatThrownBy(() -> FACTORY.weakAlias(ALIAS_NAME, aliased))
        .isInstanceOf(AssertionError.class);

    assertThatThrownBy(() -> FACTORY.strongAlias(ALIAS_NAME, aliased))
        .isInstanceOf(AssertionError.class);
  }

  private static void assertAliasImplementsTypeThroughDelegation(Type alias, Type aliased) {
    assertDelegated(Type::superType, alias, aliased);
    assertDelegated(Type::parents, alias, aliased);
    assertDelegated(Type::size, alias, aliased);
    assertDelegated(Type::isUnresolved, alias, aliased);
    assertDelegated(Type::isUntyped, alias, aliased);
    assertDelegated(Type::isUnknown, alias, aliased);
    assertDelegated(Type::isVoid, alias, aliased);
    assertDelegated(Type::isClass, alias, aliased);
    assertDelegated(Type::isInterface, alias, aliased);
    assertDelegated(Type::isRecord, alias, aliased);
    assertDelegated(Type::isEnum, alias, aliased);
    assertDelegated(Type::isSubrange, alias, aliased);
    assertDelegated(Type::isInterface, alias, aliased);
    assertDelegated(Type::isReal, alias, aliased);
    assertDelegated(Type::isString, alias, aliased);
    assertDelegated(Type::isAnsiString, alias, aliased);
    assertDelegated(Type::isChar, alias, aliased);
    assertDelegated(Type::isBoolean, alias, aliased);
    assertDelegated(Type::isStruct, alias, aliased);
    assertDelegated(Type::isFile, alias, aliased);
    assertDelegated(Type::isArray, alias, aliased);
    assertDelegated(Type::isFixedArray, alias, aliased);
    assertDelegated(Type::isDynamicArray, alias, aliased);
    assertDelegated(Type::isOpenArray, alias, aliased);
    assertDelegated(Type::isArrayOfConst, alias, aliased);
    assertDelegated(Type::isPointer, alias, aliased);
    assertDelegated(Type::isSet, alias, aliased);
    assertDelegated(Type::isProcedural, alias, aliased);
    assertDelegated(Type::isMethod, alias, aliased);
    assertDelegated(Type::isClassReference, alias, aliased);
    assertDelegated(Type::isVariant, alias, aliased);
    assertDelegated(Type::isArrayConstructor, alias, aliased);
    assertDelegated(Type::isHelper, alias, aliased);
    assertDelegated(Type::isTypeParameter, alias, aliased);

    if (aliased instanceof CollectionType) {
      assertDelegated(CollectionType::elementType, alias, aliased);
    }

    if (aliased instanceof ArrayConstructorType) {
      assertDelegated(ArrayConstructorType::elementTypes, alias, aliased);
      assertDelegated(ArrayConstructorType::isEmpty, alias, aliased);
    }

    if (aliased instanceof ScopedType) {
      assertDelegated(ScopedType::typeScope, alias, aliased);
    }

    if (aliased instanceof StructType) {
      assertDelegated(StructType::kind, alias, aliased);
      assertDelegated(StructType::attributeTypes, alias, aliased);
    }

    if (aliased instanceof HelperType) {
      assertDelegated(HelperType::extendedType, alias, aliased);
    }

    if (aliased instanceof PointerType) {
      assertDelegated(PointerType::dereferencedType, alias, aliased);
      assertDelegated(PointerType::isNilPointer, alias, aliased);
      assertDelegated(PointerType::isUntypedPointer, alias, aliased);
      assertDelegated(PointerType::allowsPointerMath, alias, aliased);
    }

    if (aliased instanceof ProceduralType) {
      assertDelegated(ProceduralType::returnType, alias, aliased);
      assertDelegated(ProceduralType::parameters, alias, aliased);
      assertDelegated(ProceduralType::parametersCount, alias, aliased);
      assertDelegated(ProceduralType::kind, alias, aliased);
    }

    if (aliased instanceof FileType) {
      assertDelegated(FileType::fileType, alias, aliased);
    }

    if (aliased instanceof SubrangeType) {
      assertDelegated(SubrangeType::hostType, alias, aliased);
    }

    if (aliased instanceof ClassReferenceType) {
      assertDelegated(ClassReferenceType::classType, alias, aliased);
    }

    if (aliased instanceof TypeParameterType) {
      assertDelegated(TypeParameterType::constraints, alias, aliased);
    }

    if (aliased instanceof IntegerType) {
      assertDelegated(IntegerType::min, alias, aliased);
      assertDelegated(IntegerType::max, alias, aliased);
      assertDelegated(IntegerType::isSigned, alias, aliased);
    }

    if (aliased instanceof StringType) {
      assertDelegated(StringType::characterType, alias, aliased);
    }

    if (aliased instanceof AnsiStringType) {
      assertDelegated(AnsiStringType::codePage, alias, aliased);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T extends Type, R> void assertDelegated(
      Function<T, R> propertyGetter, Type alias, Type aliased) {
    assertThat(propertyGetter.apply((T) alias)).isEqualTo(propertyGetter.apply((T) aliased));
  }
}
