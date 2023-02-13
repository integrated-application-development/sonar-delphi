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
package au.com.integradev.delphi.antlr.ast.visitors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import au.com.integradev.delphi.antlr.ast.DelphiNodeUtils;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import net.sourceforge.pmd.lang.ast.RootNode;
import org.junit.jupiter.api.Test;

class DelphiParserVisitorTest {

  @Test
  @SuppressWarnings("unchecked")
  void testAllNodeHandlersCallDelphiNodeHandlerByDefault() throws Exception {
    DelphiParserVisitor<?> visitor = spy(new DelphiParserVisitor<>() {});
    int expectedNumberOfVisits = 0;

    for (Method method : DelphiParserVisitor.class.getMethods()) {
      if (method.getParameterCount() != 2) {
        continue;
      }

      Class<?> paramType = method.getParameters()[0].getType();

      if (DelphiNode.class.isAssignableFrom(paramType) && DelphiNode.class != paramType) {
        method.invoke(visitor, getNodeInstance((Class<? extends DelphiNode>) paramType), null);
        ++expectedNumberOfVisits;
      }
    }

    verify(visitor, times(expectedNumberOfVisits)).visit(any(DelphiNode.class), any());
  }

  @Test
  void testAllASTNodesAreHandled() {
    Set<Class<?>> handledTypes = getTypesHandledByParserVisitor();
    Set<String> unhandledTypes =
        DelphiNodeUtils.getNodeTypes().stream()
            .filter(nodeType -> !handledTypes.contains(nodeType))
            .map(Class::getSimpleName)
            .collect(Collectors.toSet());

    if (!unhandledTypes.isEmpty()) {
      throw new AssertionError(
          String.format(
              "Expected the following types to be handled by DelphiParserVisitor: %s",
              unhandledTypes.toString()));
    }
  }

  private static Set<Class<?>> getTypesHandledByParserVisitor() {
    Method[] methods = DelphiParserVisitor.class.getDeclaredMethods();

    return Arrays.stream(methods)
        .filter(method -> method.getName().equals("visit"))
        .map(method -> method.getParameters()[0].getType())
        .collect(Collectors.toSet());
  }

  private static <T extends DelphiNode> T getNodeInstance(Class<T> clazz) {
    if (Modifier.isAbstract(clazz.getModifiers()) || RootNode.class.isAssignableFrom(clazz)) {
      return mock(clazz);
    }
    return DelphiNodeUtils.instantiateNode(clazz);
  }
}
