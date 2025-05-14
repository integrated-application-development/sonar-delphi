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

import au.com.integradev.delphi.utils.files.DelphiFileUtils;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceTypeNode;

class InterfaceTypeNodeImplTest {
  private static final String GUID = "'{B5D90CF6-B2C9-473D-9DB9-1BB75EAFC517}'";

  private static class GuidArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(
              Named.of(
                  "Simple GUID",
                  parse(
                      "TFoo = interface", //
                      "  [" + GUID + "]",
                      "end;"))),
          Arguments.of(
              Named.of(
                  "Ambiguous GUID (method attribute?)",
                  parse(
                      "TFoo = interface", //
                      "  [" + GUID + "]",
                      "  procedure Bar;",
                      "end;"))),
          Arguments.of(
              Named.of(
                  "Ambiguous GUID (property attribute?)",
                  parse(
                      "TFoo = interface", //
                      "  [" + GUID + "]",
                      "  property Bar;",
                      "end;"))),
          Arguments.of(
              Named.of(
                  "GUID within an attribute group",
                  parse(
                      "TFoo = interface",
                      "  [SomeAttribute, " + GUID + "]",
                      "  property Bar;",
                      "end;"))));
    }
  }

  private static class NoGuidArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(
              Named.of(
                  "Empty interface",
                  parse(
                      "TFoo = interface", //
                      "end;"))),
          Arguments.of(
              Named.of(
                  "Attribute declared on the first interface member",
                  parse(
                      "TFoo = interface", //
                      "  [SomeAttribute]",
                      "  procedure Bar;",
                      "end;"))),
          Arguments.of(
              Named.of(
                  "String in the second attribute group",
                  parse(
                      "TFoo = interface", //
                      "  [SomeAttribute]",
                      "  [" + GUID + "]",
                      "  procedure Bar;",
                      "end;"))),
          Arguments.of(
              Named.of(
                  "String in the first attribute group of the second attribute list",
                  parse(
                      "TFoo = interface", //
                      "  [SomeAttribute]",
                      "  procedure Bar;",
                      "  [" + GUID + "]",
                      "  procedure Baz;",
                      "end;"))));
    }
  }

  @ArgumentsSource(GuidArgumentsProvider.class)
  @ParameterizedTest
  void testGuidExpressionShouldBeFound(InterfaceTypeNode node) {
    ExpressionNode guid = node.getGuidExpression();
    assertThat(guid).isNotNull();
    assertThat(guid.getImage()).isEqualTo(GUID);
  }

  @ArgumentsSource(NoGuidArgumentsProvider.class)
  @ParameterizedTest
  void testGuidExpressionShouldNotBeFound(InterfaceTypeNode node) {
    assertThat(node.getGuidExpression()).isNull();
  }

  @SuppressWarnings("removal")
  @ArgumentsSource(GuidArgumentsProvider.class)
  @ArgumentsSource(NoGuidArgumentsProvider.class)
  @ParameterizedTest
  void testGetGuidShouldReturnNull(InterfaceTypeNode node) {
    assertThat(node.getGuid()).isNull();
  }

  private static InterfaceTypeNode parse(String... lines) {
    return DelphiFileUtils.parse(
            "unit Test;",
            "",
            "interface",
            "",
            "type",
            "  " + StringUtils.join(lines, "\n  "),
            "",
            "implementation",
            "end.")
        .getAst()
        .getFirstDescendantOfType(InterfaceTypeNode.class);
  }
}
