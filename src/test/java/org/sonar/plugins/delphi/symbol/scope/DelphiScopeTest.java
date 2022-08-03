package org.sonar.plugins.delphi.symbol.scope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.sonar.plugins.delphi.symbol.scope.DelphiScope.unknownScope;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.StructKind;
import org.sonar.plugins.delphi.utils.types.TypeMocker;

class DelphiScopeTest {
  private DelphiScope scope;

  @BeforeEach
  void setup() {
    scope = new AbstractDelphiScope() {};
  }

  private static VariableNameDeclaration createVariable() {
    return createVariable("Image");
  }

  private static VariableNameDeclaration createVariable(String image) {
    return VariableNameDeclaration.compilerVariable(image, unknownType(), unknownScope());
  }

  private static TypeNameDeclaration createType(String image) {
    return new TypeNameDeclaration(
        SymbolicNode.imaginary(image, DelphiScope.unknownScope()), DelphiType.unknownType(), image);
  }

  private static TypeNameDeclaration createClassType(String image) {
    return createClassType(image, Collections.emptyList());
  }

  private static TypeNameDeclaration createClassType(
      String image, List<TypedDeclaration> typeParameters) {
    return new TypeNameDeclaration(
        SymbolicNode.imaginary(image, DelphiScope.unknownScope()),
        TypeMocker.struct(image, StructKind.CLASS),
        image,
        typeParameters);
  }

  private static DelphiNameOccurrence createOccurrenceOf(DelphiNameDeclaration declaration) {
    var symbolicNode = SymbolicNode.imaginary(declaration.getName(), DelphiScope.unknownScope());
    DelphiNameOccurrence occurrence = new DelphiNameOccurrence(symbolicNode);
    occurrence.setNameDeclaration(declaration);
    return occurrence;
  }

  @Test
  void testContains() {
    VariableNameDeclaration declaration = createVariable();
    scope.addDeclaration(declaration);

    DelphiNameOccurrence occurrence = createOccurrenceOf(declaration);
    scope.addNameOccurrence(occurrence);

    assertThat(scope.contains(occurrence)).isTrue();

    var foo = new DelphiNameOccurrence(SymbolicNode.imaginary("Foo", unknownScope()));
    assertThat(scope.contains(foo)).isFalse();
  }

  @Test
  void testGetDeclarations() {
    VariableNameDeclaration declaration = createVariable();
    scope.addDeclaration(declaration);
    assertThat(scope.getDeclarations()).containsKey(declaration);
  }

  @Test
  void testGetDeclarationsByClass() {
    VariableNameDeclaration declaration = createVariable();
    scope.addDeclaration(declaration);
    assertThat(scope.getDeclarations(VariableNameDeclaration.class)).containsKey(declaration);
  }

  @Test
  void testVariablesWithDifferentNamesAreNotDuplicates() {
    scope.addDeclaration(createVariable("Foo"));
    scope.addDeclaration(createVariable("Bar"));
  }

  @Test
  void testTypesWithDifferentKindAndDifferentNamesAreNotDuplicates() {
    scope.addDeclaration(createType("Foo"));
    scope.addDeclaration(createType("Bar"));
  }

  @Test
  void testVariableAndTypeWithSameNameAreDuplicates() {
    scope.addDeclaration(createVariable("Foo"));

    assertThatThrownBy(() -> scope.addDeclaration(createClassType("Foo")))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  void testTypesWithDifferentKindAndSameNameAreDuplicates() {
    scope.addDeclaration(createType("Bar"));

    assertThatThrownBy(() -> scope.addDeclaration(createClassType("Bar")))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  void testForwardDeclarationsAreNotDuplicates() {
    scope.addDeclaration(createClassType("Baz"));
    scope.addDeclaration(createClassType("Baz"));
  }

  @Test
  void testGenericTypesWithDifferentNumbersOfTypeParameterAreDuplicates() {
    scope.addDeclaration(createClassType("Foo", List.of(createType("Bar"))));
    assertThatThrownBy(
            () -> scope.addDeclaration(createClassType("Foo", List.of(createType("Bar")))))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  void testGenericTypesWithSameNumberOfTypeParameterDuplicates() {
    scope.addDeclaration(createClassType("Foo"));
    scope.addDeclaration(createClassType("Foo", List.of(createType("Bar"))));
    scope.addDeclaration(createClassType("Foo", List.of(createType("Bar"), createType("Baz"))));

    scope.addDeclaration(createClassType("Bar", List.of(createType("Baz"), createType("Flarp"))));
    scope.addDeclaration(createClassType("Bar", List.of(createType("Baz"))));
    scope.addDeclaration(createClassType("Bar"));
  }

  @Test
  void testGenericTypesWithVariablesAreNotDuplicates() {
    scope.addDeclaration(createVariable("Foo"));
    scope.addDeclaration(createClassType("Foo", List.of(createType("Bar"))));

    scope.addDeclaration(createClassType("Bar", List.of(createType("Baz"))));
    scope.addDeclaration(createVariable("Bar"));
  }
}
