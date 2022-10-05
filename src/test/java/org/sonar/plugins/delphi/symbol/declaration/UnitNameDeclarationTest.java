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
package org.sonar.plugins.delphi.symbol.declaration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.antlr.runtime.CommonToken;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.FileHeaderNode;
import org.sonar.plugins.delphi.antlr.ast.node.IdentifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.QualifiedNameDeclarationNode;

class UnitNameDeclarationTest {
  @Test
  void testEquals() {
    UnitNameDeclaration foo = createUnit("Foo", "/foo.pas");
    UnitNameDeclaration otherFoo = createUnit("Foo", "/foo.pas");
    UnitNameDeclaration fooWithDifferentPath = createUnit("Foo", "/bar/foo.pas");
    UnitNameDeclaration bar = createUnit("Bar", "/bar.pas");

    assertThat(foo)
        .isEqualTo(foo)
        .isNotEqualTo(null)
        .isNotEqualTo(new Object())
        .isEqualTo(otherFoo)
        .hasSameHashCodeAs(otherFoo)
        .isNotEqualTo(fooWithDifferentPath)
        .isNotEqualByComparingTo(fooWithDifferentPath)
        .doesNotHaveSameHashCodeAs(fooWithDifferentPath)
        .isNotEqualTo(bar)
        .isNotEqualByComparingTo(bar)
        .doesNotHaveSameHashCodeAs(bar);
  }

  @Test
  void testToString() {
    UnitNameDeclaration foo = createUnit("Foo", "/foo.pas");
    assertThat(foo).hasToString("Unit Foo");
  }

  private static UnitNameDeclaration createUnit(String name, String path) {
    var identifierToken = new CommonToken(DelphiLexer.TkIdentifier, name);
    var identifierNode = new IdentifierNode(identifierToken);

    var nameNode = new QualifiedNameDeclarationNode(DelphiLexer.TkNameDeclaration);
    nameNode.jjtAddChild(identifierNode);

    DelphiAST ast = mock(DelphiAST.class);
    when(ast.getFileName()).thenReturn(path);

    FileHeaderNode location = mock(FileHeaderNode.class);
    when(location.getNameNode()).thenReturn(nameNode);
    when(location.getNamespace()).thenReturn("");
    when(location.getASTTree()).thenReturn(ast);

    return new UnitNameDeclaration(location, null);
  }
}
