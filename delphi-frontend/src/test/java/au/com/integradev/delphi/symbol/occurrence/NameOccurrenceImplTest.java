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
package au.com.integradev.delphi.symbol.occurrence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import au.com.integradev.delphi.symbol.SymbolicNode;
import com.google.common.testing.EqualsTester;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class NameOccurrenceImplTest {
  @Test
  void testIsGeneric() {
    SymbolicNode symbolicNode = SymbolicNode.imaginary("Foo", DelphiScope.unknownScope());
    NameOccurrenceImpl occurrence = new NameOccurrenceImpl(symbolicNode);

    assertThat(occurrence.isGeneric()).isFalse();

    occurrence.setIsGeneric();

    assertThat(occurrence.isGeneric()).isTrue();
  }

  @Test
  void testIsAttributeReference() {
    SymbolicNode symbolicNode = SymbolicNode.imaginary("Foo", DelphiScope.unknownScope());
    NameOccurrenceImpl occurrence = new NameOccurrenceImpl(symbolicNode);

    assertThat(occurrence.isAttributeReference()).isFalse();
  }

  @Test
  void testEquals() {
    SymbolicNode foo = SymbolicNode.imaginary("Foo", DelphiScope.unknownScope());
    NameOccurrence occurrenceA = new NameOccurrenceImpl(foo);
    NameOccurrence occurrenceB = new NameOccurrenceImpl(foo);

    SymbolicNode bar = SymbolicNode.imaginary("Bar", DelphiScope.unknownScope());
    NameOccurrence occurrenceC = new NameOccurrenceImpl(bar);

    NameOccurrenceImpl occurrenceD = new NameOccurrenceImpl(foo);
    occurrenceD.setIsExplicitInvocation(true);

    NameOccurrenceImpl occurrenceE = new NameOccurrenceImpl(foo);
    occurrenceE.setIsGeneric();

    NameOccurrenceImpl occurrenceF = new NameOccurrenceImpl(foo);
    occurrenceF.setNameDeclaration(mock(NameDeclaration.class));

    NameOccurrenceImpl occurrenceG = new NameOccurrenceImpl(foo);
    occurrenceG.setTypeArguments(List.of(TypeFactory.unknownType()));

    new EqualsTester()
        .addEqualityGroup(occurrenceA, occurrenceB)
        .addEqualityGroup(occurrenceC)
        .addEqualityGroup(occurrenceD)
        .addEqualityGroup(occurrenceE)
        .addEqualityGroup(occurrenceF)
        .addEqualityGroup(occurrenceG)
        .testEquals();
  }

  @Test
  void testToString() {
    SymbolicNode symbolicNode = SymbolicNode.imaginary("Foo", DelphiScope.unknownScope());
    assertThat(new NameOccurrenceImpl(symbolicNode)).hasToString("Foo [0,0] <<unknown unit>>");
  }
}
