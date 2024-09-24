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
package au.com.integradev.delphi.symbol.declaration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.antlr.DelphiLexer;
import au.com.integradev.delphi.antlr.ast.DelphiAstImpl;
import au.com.integradev.delphi.antlr.ast.node.EnumElementNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.EnumTypeNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.IdentifierNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.SimpleNameDeclarationNodeImpl;
import au.com.integradev.delphi.file.DelphiFile;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import com.google.common.testing.EqualsTester;
import org.antlr.runtime.CommonToken;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.EnumElementNameDeclaration;

class EnumElementNameDeclarationTest {
  @Test
  void testEquals() {
    EnumElementNameDeclaration foo = createEnumElement("Foo");
    EnumElementNameDeclaration otherFoo = createEnumElement("Foo");
    EnumElementNameDeclaration bar = createEnumElement("Bar");
    EnumElementNameDeclaration baz = createEnumElement("Baz");

    new EqualsTester()
        .addEqualityGroup(foo, otherFoo)
        .addEqualityGroup(bar)
        .addEqualityGroup(baz)
        .testEquals();

    assertThat(foo)
        .isEqualByComparingTo(otherFoo)
        .isNotEqualByComparingTo(bar)
        .isNotEqualByComparingTo(baz);
  }

  @Test
  void testToString() {
    assertThat(createEnumElement("Foo"))
        .hasToString("Enum element: image = 'Foo', line = 0 <<unknown unit>>");
  }

  private static EnumElementNameDeclaration createEnumElement(String name) {
    var identifierToken = new CommonToken(DelphiLexer.TkIdentifier, name);
    var identifierNode = new IdentifierNodeImpl(identifierToken);

    var nameNode = new SimpleNameDeclarationNodeImpl(DelphiLexer.TkNameDeclaration);
    nameNode.addChild(identifierNode);

    var element = new EnumElementNodeImpl(DelphiLexer.TkEnumElement);
    element.addChild(nameNode);

    var enumType = new EnumTypeNodeImpl(new CommonToken(DelphiLexer.PAREN_LEFT));
    enumType.addChild(element);

    DelphiFile delphiFile = mock(DelphiFile.class);
    when(delphiFile.getTypeFactory()).thenReturn(TypeFactoryUtils.defaultFactory());

    var ast = spy(new DelphiAstImpl(delphiFile, null));
    ast.addChild(enumType);

    return new EnumElementNameDeclarationImpl(element);
  }
}
