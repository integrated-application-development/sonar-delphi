/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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
package au.com.integradev.delphi.antlr.ast.node;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import au.com.integradev.delphi.symbol.occurrence.AttributeNameOccurrenceImpl;
import au.com.integradev.delphi.symbol.occurrence.NameOccurrenceImpl;
import au.com.integradev.delphi.utils.files.DelphiFileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.plugins.communitydelphi.api.ast.AttributeNode;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;

class AttributeNodeImplTest {
  @Test
  void testAttribute() {
    AttributeNode node = parse("[Foo('Bar')]");

    assertThat(node).isNotNull();
    assertThat(node.isAssembly()).isFalse();
    assertThat(node.getImage()).isEqualTo("Foo('Bar')");
    assertThat(node.getNameReference()).isNotNull();
    assertThat(node.getArgumentList()).isNotNull();
  }

  @Test
  void testAttributeNameOccurrence() {
    AttributeNode node = parse("[Foo]");

    assertThat(node.getNameReference()).isNotNull();
    assertThat(node.getTypeNameOccurrence()).isNull();
    assertThat(node.getConstructorNameOccurrence()).isNull();

    var nameReference = (NameReferenceNodeImpl) node.getNameReference();
    var constructorNameOccurrence = mock(NameOccurrence.class);
    var typeNameOccurrence = new AttributeNameOccurrenceImpl(nameReference);

    typeNameOccurrence.setImplicitConstructorNameOccurrence(constructorNameOccurrence);
    nameReference.setNameOccurrence(typeNameOccurrence);

    assertThat(node.getTypeNameOccurrence()).isEqualTo(typeNameOccurrence);
    assertThat(node.getConstructorNameOccurrence()).isEqualTo(constructorNameOccurrence);
  }

  @Test
  void testNonAttributeNameOccurrence() {
    AttributeNode node = parse("[Foo]");

    assertThat(node.getNameReference()).isNotNull();
    assertThat(node.getTypeNameOccurrence()).isNull();
    assertThat(node.getConstructorNameOccurrence()).isNull();

    var nameReference = (NameReferenceNodeImpl) node.getNameReference();
    nameReference.setNameOccurrence(new NameOccurrenceImpl(nameReference));

    assertThat(node.getTypeNameOccurrence()).isNull();
    assertThat(node.getConstructorNameOccurrence()).isNull();
  }

  @Test
  void testAssemblyAttribute() {
    AttributeNode node = parse("[assembly : Foo]");

    assertThat(node).isNotNull();
    assertThat(node.isAssembly()).isTrue();
    assertThat(node.getImage()).isEqualTo("assembly : Foo");
    assertThat(node.getNameReference()).isNotNull();
    assertThat(node.getArgumentList()).isNull();
  }

  @ValueSource(
      strings = {
        "'{B5D90CF6-B2C9-473D-9DB9-1BB75EAFC517}'",
        "'{B5D90CF6-B2C9-473D-' + '9DB9-1BB75EAFC517}'"
      })
  @ParameterizedTest
  void testExpressionAttribute(String expression) {
    AttributeNode node = parse("[" + expression + "]");

    assertThat(node).isNotNull();
    assertThat(node.isAssembly()).isFalse();
    assertThat(node.getImage()).isEqualTo(expression);
    assertThat(node.getNameReference()).isNull();
    assertThat(node.getArgumentList()).isNull();
    assertThat(node.getTypeNameOccurrence()).isNull();
    assertThat(node.getConstructorNameOccurrence()).isNull();
  }

  private static AttributeNode parse(String attribute) {
    return DelphiFileUtils.parse(
            "unit Test;",
            "",
            "interface",
            "",
            "type",
            ("  " + attribute),
            "  TFoo = record",
            "  end;",
            "",
            "implementation",
            "end.")
        .getAst()
        .getFirstDescendantOfType(AttributeNode.class);
  }
}
