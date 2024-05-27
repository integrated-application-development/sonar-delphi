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
package au.com.integradev.delphi.symbol.scope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope.unknownScope;
import static org.sonar.plugins.communitydelphi.api.type.TypeFactory.unknownType;

import au.com.integradev.delphi.symbol.SymbolicNode;
import au.com.integradev.delphi.symbol.declaration.TypeNameDeclarationImpl;
import au.com.integradev.delphi.symbol.declaration.VariableNameDeclarationImpl;
import au.com.integradev.delphi.symbol.occurrence.NameOccurrenceImpl;
import au.com.integradev.delphi.utils.types.TypeMocker;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.StructKind;

class DelphiScopeImplTest {
  private DelphiScopeImpl scope;

  @BeforeEach
  void setup() {
    scope = new DelphiScopeImpl() {};
  }

  private static VariableNameDeclaration createVariable() {
    return createVariable("Image");
  }

  private static VariableNameDeclaration createVariable(String image) {
    return VariableNameDeclarationImpl.parameter(image, unknownType(), unknownScope());
  }

  private static TypeNameDeclaration createType(String image) {
    return new TypeNameDeclarationImpl(
        SymbolicNode.imaginary(image, unknownScope()), unknownType(), image);
  }

  private static TypeNameDeclaration createClassType(String image) {
    return createClassType(image, Collections.emptyList());
  }

  private static TypeNameDeclaration createClassType(
      String image, List<TypedDeclaration> typeParameters) {
    return new TypeNameDeclarationImpl(
        SymbolicNode.imaginary(image, unknownScope()),
        TypeMocker.struct(image, StructKind.CLASS),
        image,
        typeParameters);
  }

  private static NameOccurrence createOccurrenceOf(NameDeclaration declaration) {
    var symbolicNode = SymbolicNode.imaginary(declaration.getName(), unknownScope());
    NameOccurrenceImpl occurrence = new NameOccurrenceImpl(symbolicNode);
    occurrence.setNameDeclaration(declaration);
    return occurrence;
  }

  @Test
  void testFindDeclaration() {
    VariableNameDeclaration declaration = createVariable();
    scope.addDeclaration(declaration);

    NameOccurrence occurrence = createOccurrenceOf(declaration);
    scope.addNameOccurrence(occurrence);

    assertThat(scope.findDeclaration(occurrence)).isNotEmpty();

    var foo = new NameOccurrenceImpl(SymbolicNode.imaginary("Foo", unknownScope()));
    assertThat(scope.findDeclaration(foo)).isEmpty();
  }

  static Stream<Arguments> distinctIdentifiers() {
    return Stream.of(Arguments.of("Foo", "Fob"), Arguments.of("ﬂ", "FL"));
  }

  @ParameterizedTest
  @MethodSource({"distinctIdentifiers", "distinctIdentifierShortcomings"})
  void testVariablesWithDifferentNamesAreNotDuplicates(String first, String second) {
    scope.addDeclaration(createVariable(first));

    VariableNameDeclaration bar = createVariable(second);
    assertThatCode(() -> scope.addDeclaration(bar)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @MethodSource({"distinctIdentifiers", "distinctIdentifierShortcomings"})
  void testTypesWithDifferentKindAndDifferentNamesAreNotDuplicates(String first, String second) {
    scope.addDeclaration(createType(first));

    TypeNameDeclaration bar = createType(second);
    assertThatCode(() -> scope.addDeclaration(bar)).doesNotThrowAnyException();
  }

  static Stream<Arguments> duplicateIdentifiers() {
    return Stream.of(
        Arguments.of("Foo", "Foo"),
        Arguments.of("Foo", "foo"),
        Arguments.of("Foo", "FOO"),
        Arguments.of("À", "à"),
        Arguments.of("Δ", "δ"),
        Arguments.of("Ⅰ", "ⅰ"));
  }

  // According to the Unicode standard, these should be considered as the same identifier since they
  // are case-folded to the same string. However, the Delphi compiler considers them distinct. On
  // the other hand, java's String::compareIgnoreCase knows that they are equivalent.
  //
  // We don't feel that it's worth accounting for obscure cases like these, because it would mean
  // that the 'obvious' way of comparing case-insensitively in this codebase would be incorrect.
  // Even if we did feel like it was worth it, we would still be guessing what the 'real' rules are,
  // because there is no documentation from Embarcadero on exactly how the case-insensitive
  // comparison is performed.
  static Stream<Arguments> duplicateIdentifierShortcomings() {
    return Stream.of(Arguments.of("ẞ", "ß"), Arguments.of("ſ", "s"));
  }

  // Delphi actually considers all these identifiers are duplicates. Certain WinAPI calls agree with
  // this conclusion (e.g. CompareStringEx (any flags) and CompareStringW (some locales)).
  // As above, we aren't going to account for these cases right now.
  static Stream<Arguments> distinctIdentifierShortcomings() {
    return Stream.of(Arguments.of("힢", "ퟂ"), Arguments.of("힢", "ퟢ"), Arguments.of("ퟂ", "ퟢ"));
  }

  @ParameterizedTest
  @MethodSource({"duplicateIdentifiers", "duplicateIdentifierShortcomings"})
  void testVariableAndTypeWithSameNameAreDuplicates(String first, String second) {
    scope.addDeclaration(createVariable(first));

    TypeNameDeclaration classType = createClassType(second);
    assertThatThrownBy(() -> scope.addDeclaration(classType))
        .isInstanceOf(DuplicatedDeclarationException.class);
  }

  @ParameterizedTest
  @MethodSource({"duplicateIdentifiers", "duplicateIdentifierShortcomings"})
  void testTypesWithDifferentKindAndSameNameAreDuplicates(String first, String second) {
    scope.addDeclaration(createType(first));

    TypeNameDeclaration classType = createClassType(second);
    assertThatThrownBy(() -> scope.addDeclaration(classType))
        .isInstanceOf(DuplicatedDeclarationException.class);
  }

  @Test
  void testForwardDeclarationsAreNotDuplicates() {
    scope.addDeclaration(createClassType("Baz"));

    TypeNameDeclaration baz = createClassType("Baz");
    assertThatCode(() -> scope.addDeclaration(baz)).doesNotThrowAnyException();
  }

  @Test
  void testGenericTypesWithSameNumberOfTypeParametersAreNotDuplicates() {
    scope.addDeclaration(createClassType("Foo", List.of(createType("Bar"))));
    assertThatCode(() -> scope.addDeclaration(createClassType("Foo", List.of(createType("Bar")))))
        .doesNotThrowAnyException();
  }

  @Test
  void testGenericTypesWithDifferentNumberOfTypeParametersAreNotDuplicates() {
    scope.addDeclaration(createClassType("Foo"));
    scope.addDeclaration(createClassType("Foo", List.of(createType("Bar"))));

    TypeNameDeclaration foo = createClassType("Foo", List.of(createType("Bar"), createType("Baz")));
    assertThatCode(() -> scope.addDeclaration(foo)).doesNotThrowAnyException();

    scope.addDeclaration(createClassType("Bar", List.of(createType("Baz"), createType("Flarp"))));
    scope.addDeclaration(createClassType("Bar", List.of(createType("Baz"))));

    TypeNameDeclaration bar = createClassType("Bar");
    assertThatCode(() -> scope.addDeclaration(bar)).doesNotThrowAnyException();
  }

  @Test
  void testGenericTypesWithVariablesAreNotDuplicates() {
    scope.addDeclaration(createVariable("Foo"));

    TypeNameDeclaration foo = createClassType("Foo", List.of(createType("Bar")));
    assertThatCode(() -> scope.addDeclaration(foo)).doesNotThrowAnyException();

    scope.addDeclaration(createClassType("Bar", List.of(createType("Baz"))));

    VariableNameDeclaration bar = createVariable("Bar");
    assertThatCode(() -> scope.addDeclaration(bar)).doesNotThrowAnyException();
  }
}
