package org.sonar.plugins.delphi.symbol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.type.DelphiType;

class DelphiNameOccurrenceTest {
  @Test
  void testQualifiedName() {
    SymbolicNode foo = SymbolicNode.imaginary("Foo", DelphiScope.unknownScope());
    DelphiNameOccurrence occurrenceA = new DelphiNameOccurrence(foo);

    SymbolicNode bar = SymbolicNode.imaginary("Bar", DelphiScope.unknownScope());
    DelphiNameOccurrence occurrenceB = new DelphiNameOccurrence(bar);

    assertThat(occurrenceA.isPartOfQualifiedName()).isFalse();
    assertThat(occurrenceA.getNameForWhichThisIsAQualifier()).isNull();

    occurrenceA.setNameWhichThisQualifies(occurrenceB);

    assertThat(occurrenceA.isPartOfQualifiedName()).isTrue();
    assertThat(occurrenceA.getNameForWhichThisIsAQualifier()).isEqualTo(occurrenceB);
  }

  @Test
  void testIsMethodReference() {
    SymbolicNode symbolicNode = SymbolicNode.imaginary("Foo", DelphiScope.unknownScope());
    DelphiNameOccurrence occurrence = new DelphiNameOccurrence(symbolicNode);

    assertThat(occurrence.isMethodReference()).isFalse();

    occurrence.setIsMethodReference();

    assertThat(occurrence.isMethodReference()).isTrue();
  }

  @Test
  void testIsGeneric() {
    SymbolicNode symbolicNode = SymbolicNode.imaginary("Foo", DelphiScope.unknownScope());
    DelphiNameOccurrence occurrence = new DelphiNameOccurrence(symbolicNode);

    assertThat(occurrence.isGeneric()).isFalse();

    occurrence.setIsGeneric();

    assertThat(occurrence.isGeneric()).isTrue();
  }

  @Test
  void testIsSelf() {
    SymbolicNode symbolicNode = SymbolicNode.imaginary("Self", DelphiScope.unknownScope());
    DelphiNameOccurrence occurrence = new DelphiNameOccurrence(symbolicNode);
    assertThat(occurrence.isSelf()).isTrue();
  }

  @Test
  void testEquals() {
    SymbolicNode foo = SymbolicNode.imaginary("Foo", DelphiScope.unknownScope());

    DelphiNameOccurrence occurrenceA = new DelphiNameOccurrence(foo);
    assertThat(occurrenceA).isEqualTo(occurrenceA).isNotEqualTo(null).isNotEqualTo(new Object());

    DelphiNameOccurrence occurrenceB = new DelphiNameOccurrence(foo);
    assertThat(occurrenceA).isEqualTo(occurrenceB);

    SymbolicNode bar = SymbolicNode.imaginary("Bar", DelphiScope.unknownScope());
    DelphiNameOccurrence occurrenceC = new DelphiNameOccurrence(bar);
    assertThat(occurrenceA).isNotEqualTo(occurrenceC);

    DelphiNameOccurrence occurrenceD = new DelphiNameOccurrence(foo);
    occurrenceD.setIsExplicitInvocation(true);
    assertThat(occurrenceA).isNotEqualTo(occurrenceD);

    DelphiNameOccurrence occurrenceE = new DelphiNameOccurrence(foo);
    occurrenceE.setIsMethodReference();
    assertThat(occurrenceA).isNotEqualTo(occurrenceE);

    DelphiNameOccurrence occurrenceF = new DelphiNameOccurrence(foo);
    occurrenceF.setIsGeneric();
    assertThat(occurrenceA).isNotEqualTo(occurrenceF);

    DelphiNameOccurrence occurrenceG = new DelphiNameOccurrence(foo);
    occurrenceG.setNameDeclaration(mock(DelphiNameDeclaration.class));
    assertThat(occurrenceA).isNotEqualTo(occurrenceG);

    DelphiNameOccurrence occurrenceH = new DelphiNameOccurrence(foo);
    occurrenceH.setNameWhichThisQualifies(new DelphiNameOccurrence(bar));
    assertThat(occurrenceA).isNotEqualTo(occurrenceH);

    DelphiNameOccurrence occurrenceI = new DelphiNameOccurrence(foo);
    occurrenceI.setTypeArguments(List.of(DelphiType.unknownType()));
    assertThat(occurrenceA).isNotEqualTo(occurrenceI);
  }

  @Test
  void testToString() {
    SymbolicNode symbolicNode = SymbolicNode.imaginary("Foo", DelphiScope.unknownScope());
    assertThat(new DelphiNameOccurrence(symbolicNode)).hasToString("Foo [0,0] <<unknown unit>>");
  }
}
