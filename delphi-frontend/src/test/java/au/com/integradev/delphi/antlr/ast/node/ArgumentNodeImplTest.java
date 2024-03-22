/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.antlr.DelphiLexer;
import javax.annotation.Nullable;
import org.antlr.runtime.CommonToken;
import org.junit.jupiter.api.Test;

class ArgumentNodeImplTest {
  @Test
  void testGetImage() {
    assertThat(getImage("Foo", "Bar", "Baz", "Flarp")).isEqualTo("Foo := Bar:Baz:Flarp");
    assertThat(getImage(null, "Foo", "Bar", "Baz")).isEqualTo("Foo:Bar:Baz");
    assertThat(getImage(null, "Foo", "Bar", null)).isEqualTo("Foo:Bar");
    assertThat(getImage(null, "Foo", null, null)).isEqualTo("Foo");
  }

  private static String getImage(
      @Nullable String nameImage,
      String expressionImage,
      @Nullable String widthImage,
      @Nullable String decimalImage) {
    return createArgumentNode(nameImage, expressionImage, widthImage, decimalImage).getImage();
  }

  private static ArgumentNodeImpl createArgumentNode(
      @Nullable String nameImage,
      String expressionImage,
      @Nullable String widthImage,
      @Nullable String decimalImage) {
    ArgumentNodeImpl node = new ArgumentNodeImpl(DelphiLexer.TkArgument);
    if (nameImage != null) {
      var name = new IdentifierNodeImpl(new CommonToken(DelphiLexer.TkIdentifier, nameImage));
      node.addChild(name);
    }

    node.addChild(mockExpression(expressionImage));

    if (widthImage != null) {
      node.addChild(mockExpression(widthImage));
    }

    if (decimalImage != null) {
      node.addChild(mockExpression(decimalImage));
    }

    return node;
  }

  private static ExpressionNodeImpl mockExpression(String image) {
    ExpressionNodeImpl expression = mock();
    when(expression.getToken()).thenReturn(mock());
    when(expression.getImage()).thenReturn(image);
    return expression;
  }
}
