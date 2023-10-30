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
package au.com.integradev.delphi.antlr.ast.visitors;

import static au.com.integradev.delphi.antlr.ast.DelphiNodeUtils.implType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.antlr.ast.DelphiNodeUtils;
import au.com.integradev.delphi.antlr.ast.node.DelphiNodeImpl;
import au.com.integradev.delphi.file.DelphiFile;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.ModifierSupport;
import org.mockito.Answers;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

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
        DelphiNodeUtils.getNodeInterfaceTypes().stream()
            .filter(nodeType -> !handledTypes.contains(nodeType))
            .map(Class::getSimpleName)
            .collect(Collectors.toSet());

    if (!unhandledTypes.isEmpty()) {
      throw new AssertionError(
          String.format(
              "Expected the following types to be handled by DelphiParserVisitor: %s",
              unhandledTypes));
    }
  }

  private static Set<Class<?>> getTypesHandledByParserVisitor() {
    Method[] methods = DelphiParserVisitor.class.getDeclaredMethods();

    return Arrays.stream(methods)
        .filter(method -> method.getName().equals("visit"))
        .map(method -> method.getParameters()[0].getType())
        .collect(Collectors.toSet());
  }

  @SuppressWarnings("unchecked")
  private static <T extends DelphiNode> T getNodeInstance(Class<T> clazz) {
    if (DelphiAst.class.isAssignableFrom(clazz)) {
      DelphiFile delphiFile = mock(Answers.RETURNS_DEEP_STUBS);

      DelphiAst ast = (DelphiAst) mock(clazz);
      when(ast.getDelphiFile()).thenReturn(delphiFile);

      return clazz.cast(ast);
    }

    Class<? extends DelphiNodeImpl> implType = implType(clazz);
    if (implType == null) {
      return mock(clazz);
    }

    if (ModifierSupport.isAbstract(implType)) {
      return (T) mock(implType);
    }

    return (T) DelphiNodeUtils.instantiateNode(implType);
  }
}
