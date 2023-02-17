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

import au.com.integradev.delphi.type.factory.TypeFactory;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;

class NameOccurrenceImplTest {
  @Test
  void testQualifiedName() {
    SymbolicNode foo = SymbolicNode.imaginary("Foo", DelphiScope.unknownScope());
    NameOccurrenceImpl occurrenceA = new NameOccurrenceImpl(foo);

    SymbolicNode bar = SymbolicNode.imaginary("Bar", DelphiScope.unknownScope());
    NameOccurrence occurrenceB = new NameOccurrenceImpl(bar);

    assertThat(occurrenceA.isPartOfQualifiedName()).isFalse();
    assertThat(occurrenceA.getNameForWhichThisIsAQualifier()).isNull();

    occurrenceA.setNameWhichThisQualifies(occurrenceB);

    assertThat(occurrenceA.isPartOfQualifiedName()).isTrue();
    assertThat(occurrenceA.getNameForWhichThisIsAQualifier()).isEqualTo(occurrenceB);
  }

  @Test
  void testIsMethodReference() {
    SymbolicNode symbolicNode = SymbolicNode.imaginary("Foo", DelphiScope.unknownScope());
    NameOccurrenceImpl occurrence = new NameOccurrenceImpl(symbolicNode);

    assertThat(occurrence.isMethodReference()).isFalse();

    occurrence.setIsMethodReference();

    assertThat(occurrence.isMethodReference()).isTrue();
  }

  @Test
  void testIsGeneric() {
    SymbolicNode symbolicNode = SymbolicNode.imaginary("Foo", DelphiScope.unknownScope());
    NameOccurrenceImpl occurrence = new NameOccurrenceImpl(symbolicNode);

    assertThat(occurrence.isGeneric()).isFalse();

    occurrence.setIsGeneric();

    assertThat(occurrence.isGeneric()).isTrue();
  }

  @Test
  void testIsSelf() {
    SymbolicNode symbolicNode = SymbolicNode.imaginary("Self", DelphiScope.unknownScope());
    NameOccurrence occurrence = new NameOccurrenceImpl(symbolicNode);
    assertThat(occurrence.isSelf()).isTrue();
  }

  @Test
  void testEquals() {
    SymbolicNode foo = SymbolicNode.imaginary("Foo", DelphiScope.unknownScope());

    NameOccurrence occurrenceA = new NameOccurrenceImpl(foo);
    assertThat(occurrenceA).isEqualTo(occurrenceA).isNotEqualTo(null).isNotEqualTo(new Object());

    NameOccurrence occurrenceB = new NameOccurrenceImpl(foo);
    assertThat(occurrenceA).isEqualTo(occurrenceB);

    SymbolicNode bar = SymbolicNode.imaginary("Bar", DelphiScope.unknownScope());
    NameOccurrence occurrenceC = new NameOccurrenceImpl(bar);
    assertThat(occurrenceA).isNotEqualTo(occurrenceC);

    NameOccurrenceImpl occurrenceD = new NameOccurrenceImpl(foo);
    occurrenceD.setIsExplicitInvocation(true);
    assertThat(occurrenceA).isNotEqualTo(occurrenceD);

    NameOccurrenceImpl occurrenceE = new NameOccurrenceImpl(foo);
    occurrenceE.setIsMethodReference();
    assertThat(occurrenceA).isNotEqualTo(occurrenceE);

    NameOccurrenceImpl occurrenceF = new NameOccurrenceImpl(foo);
    occurrenceF.setIsGeneric();
    assertThat(occurrenceA).isNotEqualTo(occurrenceF);

    NameOccurrenceImpl occurrenceG = new NameOccurrenceImpl(foo);
    occurrenceG.setNameDeclaration(mock(NameDeclaration.class));
    assertThat(occurrenceA).isNotEqualTo(occurrenceG);

    NameOccurrenceImpl occurrenceH = new NameOccurrenceImpl(foo);
    occurrenceH.setNameWhichThisQualifies(new NameOccurrenceImpl(bar));
    assertThat(occurrenceA).isNotEqualTo(occurrenceH);

    NameOccurrenceImpl occurrenceI = new NameOccurrenceImpl(foo);
    occurrenceI.setTypeArguments(List.of(TypeFactory.unknownType()));
    assertThat(occurrenceA).isNotEqualTo(occurrenceI);
  }

  @Test
  void testToString() {
    SymbolicNode symbolicNode = SymbolicNode.imaginary("Foo", DelphiScope.unknownScope());
    assertThat(new NameOccurrenceImpl(symbolicNode)).hasToString("Foo [0,0] <<unknown unit>>");
  }
}
