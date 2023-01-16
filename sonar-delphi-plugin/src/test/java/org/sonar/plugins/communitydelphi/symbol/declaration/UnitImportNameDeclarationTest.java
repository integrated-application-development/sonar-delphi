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
package org.sonar.plugins.communitydelphi.symbol.declaration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.antlr.runtime.CommonToken;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.antlr.DelphiLexer;
import org.sonar.plugins.communitydelphi.antlr.ast.DelphiAST;
import org.sonar.plugins.communitydelphi.antlr.ast.node.FileHeaderNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.IdentifierNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.QualifiedNameDeclarationNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.UnitImportNode;

class UnitImportNameDeclarationTest {
  @Test
  void testEquals() {
    UnitImportNameDeclaration foo = createImport("Foo");
    UnitImportNameDeclaration otherFoo = createImport("Foo");
    UnitImportNameDeclaration differentName = createImport("Bar");
    UnitImportNameDeclaration differentOriginalDeclaration = createImport("Foo", createUnit("Foo"));

    assertThat(foo)
        .isEqualTo(foo)
        .isNotEqualTo(null)
        .isNotEqualTo(new Object())
        .isEqualTo(otherFoo)
        .isEqualByComparingTo(otherFoo)
        .hasSameHashCodeAs(otherFoo)
        .isNotEqualTo(differentName)
        .doesNotHaveSameHashCodeAs(differentName)
        .isNotEqualTo(differentOriginalDeclaration)
        .doesNotHaveSameHashCodeAs(differentOriginalDeclaration);
  }

  @Test
  void testToString() {
    assertThat(createImport("Foo")).hasToString("Import Foo");
  }

  private static UnitImportNameDeclaration createImport(String name) {
    return createImport(name, null);
  }

  private static UnitImportNameDeclaration createImport(
      String name, UnitNameDeclaration originalDeclaration) {
    UnitImportNode location = new UnitImportNode(DelphiLexer.TkUnitImport);
    location.jjtAddChild(createNameNode(name));
    return new UnitImportNameDeclaration(location, originalDeclaration);
  }

  private static UnitNameDeclaration createUnit(String name) {
    DelphiAST ast = mock(DelphiAST.class);
    when(ast.getFileName()).thenReturn(name + ".pas");

    FileHeaderNode location = mock(FileHeaderNode.class);
    when(location.getNameNode()).thenReturn(createNameNode(name));
    when(location.getNamespace()).thenReturn("");
    when(location.getASTTree()).thenReturn(ast);

    return new UnitNameDeclaration(location, null);
  }

  private static QualifiedNameDeclarationNode createNameNode(String name) {
    var identifierToken = new CommonToken(DelphiLexer.TkIdentifier, name);
    var identifierNode = new IdentifierNode(identifierToken);

    var nameNode = new QualifiedNameDeclarationNode(DelphiLexer.TkNameDeclaration);
    nameNode.jjtAddChild(identifierNode);

    return nameNode;
  }
}
