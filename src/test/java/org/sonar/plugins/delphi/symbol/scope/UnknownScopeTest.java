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
package org.sonar.plugins.delphi.symbol.scope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration.compilerVariable;
import static org.sonar.plugins.delphi.symbol.scope.DelphiScope.unknownScope;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;

class UnknownScopeTest {
  private final UnknownScope unknownScope = unknownScope();

  @Test
  void testConstructorIsPrivate() throws Exception {
    Constructor<UnknownScope> constructor = UnknownScope.class.getDeclaredConstructor();
    assertThat(constructor.canAccess(null)).isFalse();
  }

  @Test
  void testGetEnclosingScope() {
    unknownScope.setParent(unknownScope());
    assertThat(unknownScope.getEnclosingScope(UnknownScope.class)).isNull();
  }

  @Test
  void testGetDeclarations() {
    unknownScope.addDeclaration(mock(NameDeclaration.class));
    assertThat(unknownScope.getDeclarations()).isEmpty();
  }

  @Test
  void testGetAllDeclarations() {
    unknownScope.addDeclaration(mock(NameDeclaration.class));
    assertThat(unknownScope.getAllDeclarations()).isEmpty();
  }

  @Test
  void testGetDeclarationsByClass() {
    var declaration = compilerVariable("Image", unknownType(), unknownScope());
    unknownScope.addDeclaration(declaration);
    assertThat(unknownScope.getDeclarations(VariableNameDeclaration.class)).isEmpty();
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

  private static DelphiNameOccurrence makeNameOccurrence() {
    DelphiNode location = mock(DelphiNode.class);
    when(location.getScope()).thenReturn(unknownScope());
    return new DelphiNameOccurrence(location, "Image");
  }
}
