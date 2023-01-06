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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.antlr.runtime.CommonToken;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.EnumElementNode;
import org.sonar.plugins.delphi.antlr.ast.node.EnumTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.IdentifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.SimpleNameDeclarationNode;
import org.sonar.plugins.delphi.file.DelphiFile;
import org.sonar.plugins.delphi.utils.types.TypeFactoryUtils;

class EnumElementNameDeclarationTest {
  @Test
  void testEquals() {
    EnumElementNameDeclaration foo = createEnumElement("Foo");
    EnumElementNameDeclaration otherFoo = createEnumElement("Foo");
    EnumElementNameDeclaration bar = createEnumElement("Bar");

    assertThat(foo)
        .isEqualTo(foo)
        .isNotEqualTo(null)
        .isNotEqualTo(new Object())
        .isEqualTo(otherFoo)
        .isEqualByComparingTo(otherFoo)
        .hasSameHashCodeAs(otherFoo)
        .isNotEqualTo(bar)
        .isNotEqualByComparingTo(bar)
        .doesNotHaveSameHashCodeAs(bar);
  }

  @Test
  void testToString() {
    assertThat(createEnumElement("Foo"))
        .hasToString("Enum element: image = 'Foo', line = 0 <<unknown unit>>");
  }

  private static EnumElementNameDeclaration createEnumElement(String name) {
    var identifierToken = new CommonToken(DelphiLexer.TkIdentifier, name);
    var identifierNode = new IdentifierNode(identifierToken);

    var nameNode = new SimpleNameDeclarationNode(DelphiLexer.TkNameDeclaration);
    nameNode.jjtAddChild(identifierNode);

    EnumElementNode element = new EnumElementNode(DelphiLexer.TkEnumElement);
    element.jjtAddChild(nameNode);

    EnumTypeNode enumType = new EnumTypeNode(new CommonToken(DelphiLexer.LPAREN));
    enumType.jjtAddChild(element);

    DelphiFile delphiFile = mock(DelphiFile.class);
    when(delphiFile.getTypeFactory()).thenReturn(TypeFactoryUtils.defaultFactory());

    DelphiAST ast = spy(new DelphiAST(delphiFile, null));
    ast.jjtAddChild(enumType);

    return new EnumElementNameDeclaration(element);
  }
}
