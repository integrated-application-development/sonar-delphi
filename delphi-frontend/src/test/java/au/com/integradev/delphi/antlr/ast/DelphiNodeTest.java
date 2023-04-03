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
package au.com.integradev.delphi.antlr.ast;

import static au.com.integradev.delphi.antlr.ast.DelphiNodeUtils.ARE_DELPHI_NODES;
import static au.com.integradev.delphi.antlr.ast.DelphiNodeUtils.HAVE_TOKEN_CONSTRUCTOR;
import static au.com.integradev.delphi.antlr.ast.DelphiNodeUtils.IMPLEMENT_ACCEPT;
import static au.com.integradev.delphi.antlr.ast.DelphiNodeUtils.NODE_IMPL_PACKAGE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

class DelphiNodeTest {
  @Test
  void testAllNodesShouldBeAbstractOrFinal() {
    classes()
        .that(ARE_DELPHI_NODES)
        .should()
        .haveModifier(JavaModifier.ABSTRACT)
        .orShould()
        .haveModifier(JavaModifier.FINAL)
        .check(NODE_IMPL_PACKAGE);
  }

  @Test
  void testAllNodesShouldEndNameWithNodeImpl() {
    classes()
        .that(ARE_DELPHI_NODES)
        .should()
        .haveSimpleNameEndingWith("NodeImpl")
        .check(NODE_IMPL_PACKAGE);
  }

  @Test
  void testAbstractNodesShouldNotImplementAccept() {
    classes()
        .that(ARE_DELPHI_NODES)
        .and(IMPLEMENT_ACCEPT)
        .should()
        .notHaveModifier(JavaModifier.ABSTRACT)
        .check(NODE_IMPL_PACKAGE);
  }

  @Test
  void testAcceptShouldCallCorrectVisitorMethod() {
    for (DelphiNode node : DelphiNodeUtils.getNodeInstances()) {
      DelphiParserVisitor<?> visitor = new DelphiParserVisitor<>() {};
      DelphiParserVisitor<?> visitorSpy = spy(visitor);
      node.accept(visitorSpy, null);

      assertCorrectVisitMethodCalled(visitor, visitorSpy, node);
    }
  }

  private static void assertCorrectVisitMethodCalled(
      DelphiParserVisitor<?> visitor, DelphiParserVisitor<?> visitorSpy, DelphiNode node) {
    try {
      Optional<Class<?>> interfaceType =
          Arrays.stream(node.getClass().getInterfaces())
              .filter(intf -> node.getClass().getSimpleName().equals(intf.getSimpleName() + "Impl"))
              .findFirst();

      assertThat(interfaceType).isPresent();

      final Object verify = verify(visitorSpy);
      Method method = visitor.getClass().getMethod("visit", interfaceType.get(), Object.class);
      method.invoke(verify, node, null);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  @Test
  void testAllNodesHaveTokenConstructor() {
    classes()
        .that(ARE_DELPHI_NODES)
        .should(HAVE_TOKEN_CONSTRUCTOR)
        .because("this is needed for reflective instantiation")
        .check(NODE_IMPL_PACKAGE);
  }
}
