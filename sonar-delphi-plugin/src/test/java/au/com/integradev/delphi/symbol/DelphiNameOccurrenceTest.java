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
package au.com.integradev.delphi.symbol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import au.com.integradev.delphi.symbol.declaration.DelphiNameDeclaration;
import au.com.integradev.delphi.symbol.scope.DelphiScope;
import au.com.integradev.delphi.type.DelphiType;
import java.util.List;
import org.junit.jupiter.api.Test;

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
