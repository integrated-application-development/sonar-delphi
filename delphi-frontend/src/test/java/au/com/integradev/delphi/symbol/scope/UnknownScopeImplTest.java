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
package au.com.integradev.delphi.symbol.scope;

import static au.com.integradev.delphi.symbol.declaration.VariableNameDeclarationImpl.compilerVariable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope.unknownScope;
import static org.sonar.plugins.communitydelphi.api.type.TypeFactory.unknownType;

import au.com.integradev.delphi.symbol.NameOccurrenceImpl;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;

class UnknownScopeImplTest {
  private final UnknownScopeImpl unknownScope = UnknownScopeImpl.instance();

  @Test
  void testConstructorIsPrivate() throws Exception {
    Constructor<UnknownScopeImpl> constructor = UnknownScopeImpl.class.getDeclaredConstructor();
    assertThat(constructor.canAccess(null)).isFalse();
  }

  @Test
  void testGetEnclosingScope() {
    unknownScope.setParent(unknownScope());
    assertThat(unknownScope.getEnclosingScope(UnknownScopeImpl.class)).isNull();
  }

  @Test
  void testGetAllDeclarations() {
    unknownScope.addDeclaration(mock(NameDeclaration.class));
    assertThat(unknownScope.getAllDeclarations()).isEmpty();
  }

  @Test
  void testContains() {
    VariableNameDeclaration declaration = compilerVariable("Image", unknownType(), unknownScope);
    unknownScope.addDeclaration(declaration);
    assertThat(unknownScope.contains(makeNameOccurrence())).isFalse();
  }

  @Test
  void testAddNameOccurrence() {
    VariableNameDeclaration declaration = compilerVariable("Image", unknownType(), unknownScope);
    unknownScope.addDeclaration(declaration);
    assertThat(unknownScope.addNameOccurrence(makeNameOccurrence())).isEmpty();
  }

  @Test
  void testFindDeclaration() {
    VariableNameDeclaration declaration = compilerVariable("Image", unknownType(), unknownScope);
    unknownScope.addDeclaration(declaration);
    assertThat(unknownScope.findDeclaration(makeNameOccurrence())).isEmpty();
  }

  @Test
  void testGetOccurrencesFor() {
    VariableNameDeclaration declaration = compilerVariable("Image", unknownType(), unknownScope);
    unknownScope.addDeclaration(declaration);
    assertThat(unknownScope.addNameOccurrence(makeNameOccurrence())).isEmpty();
    assertThat(unknownScope.getOccurrencesFor(declaration)).isEmpty();
  }

  @Test
  void testFindMethodOverloads() {
    Set<NameDeclaration> result = new HashSet<>();
    unknownScope.findMethodOverloads(makeNameOccurrence(), result);
    assertThat(result).isEmpty();
  }

  @Test
  void testGetParent() {
    unknownScope.setParent(unknownScope());
    assertThat(unknownScope.getParent()).isNull();
  }

  @Test
  void testGetHelperForType() {
    assertThat(unknownScope.getHelperForType(unknownType())).isNull();
  }

  @Test
  void testGetUnitDeclarations() {
    assertThat(unknownScope.getUnitDeclarations()).isEmpty();
  }

  @Test
  void testGetImportDeclarations() {
    assertThat(unknownScope.getImportDeclarations()).isEmpty();
  }

  @Test
  void testGetTypeDeclarations() {
    assertThat(unknownScope.getTypeDeclarations()).isEmpty();
  }

  @Test
  void testGetPropertyDeclarations() {
    assertThat(unknownScope.getPropertyDeclarations()).isEmpty();
  }

  @Test
  void testGetMethodDeclarations() {
    assertThat(unknownScope.getMethodDeclarations()).isEmpty();
  }

  @Test
  void testGetVariableDeclarations() {
    assertThat(unknownScope.getVariableDeclarations()).isEmpty();
  }

  private static NameOccurrence makeNameOccurrence() {
    DelphiNode location = mock(DelphiNode.class);
    when(location.getScope()).thenReturn(unknownScope());
    return new NameOccurrenceImpl(location, "Image");
  }
}
