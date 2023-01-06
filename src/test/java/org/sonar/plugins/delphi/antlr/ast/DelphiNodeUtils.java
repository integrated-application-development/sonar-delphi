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
package org.sonar.plugins.delphi.antlr.ast;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClass.Predicates;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvent;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class DelphiNodeUtils {
  public static final JavaClasses NODE_PACKAGE =
      new ClassFileImporter().importPackages("org.sonar.plugins.delphi.antlr.ast.node");

  public static final DescribedPredicate<JavaClass> ARE_DELPHI_NODES =
      DescribedPredicate.describe(
          "inherit from DelphiNode", Predicates.assignableTo(DelphiNode.class));

  public static final DescribedPredicate<JavaClass> IMPLEMENT_ACCEPT =
      new DescribedPredicate<>("have a method that implements accept(DelphiParserVisitor, T)") {
        private final Class<?>[] ACCEPT_PARAMETERS = {DelphiParserVisitor.class, Object.class};

        @Override
        public boolean apply(JavaClass javaClass) {
          return javaClass.getMethods().stream().anyMatch(this::isAcceptMethodImplementation);
        }

        private boolean isAcceptMethodImplementation(JavaMethod javaMethod) {
          Method method = javaMethod.reflect();
          return method.getName().equals("accept")
              && Arrays.equals(method.getParameterTypes(), ACCEPT_PARAMETERS)
              && !Modifier.isAbstract(method.getModifiers());
        }
      };

  public static final ArchCondition<JavaClass> HAVE_TOKEN_CONSTRUCTOR =
      new ArchCondition<>("have a public constructor(Token)") {
        private final Class<?>[] TOKEN_PARAMETER = {Token.class};

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
          boolean satisfied =
              javaClass.getConstructors().stream()
                  .map(JavaConstructor::reflect)
                  .anyMatch(
                      constructor ->
                          Arrays.equals(constructor.getParameterTypes(), TOKEN_PARAMETER));

          String message =
              javaClass
                  + (satisfied ? " has the " : " does not have the ")
                  + "expected constructor.";

          ConditionEvent event = new SimpleConditionEvent(javaClass, satisfied, message);

          events.add(event);
        }
      };

  private DelphiNodeUtils() {
    // Utility class
  }

  @SuppressWarnings("unchecked")
  public static Set<Class<? extends DelphiNode>> getNodeTypes() {
    return NODE_PACKAGE.stream()
        .filter(clazz -> clazz.isAssignableTo(DelphiNode.class))
        .map(clazz -> (Class<? extends DelphiNode>) clazz.reflect())
        .collect(Collectors.toUnmodifiableSet());
  }

  public static Set<? extends DelphiNode> getNodeInstances() {
    return getNodeTypes().stream()
        .filter(nodeType -> !Modifier.isAbstract(nodeType.getModifiers()))
        .map(DelphiNodeUtils::instantiateNode)
        .collect(Collectors.toSet());
  }

  public static <T extends DelphiNode> T instantiateNode(Class<T> nodeType) {
    Object instance;

    try {
      Constructor<?> constructor = nodeType.getConstructor(Token.class);
      instance = constructor.newInstance(Token.INVALID_TOKEN);
      assertThat(instance)
          .as(
              "Could not instantiate %s. Requires public constructor(Token)",
              nodeType.getSimpleName())
          .isNotNull();
    } catch (Exception e) {
      throw new AssertionError(e);
    }

    return nodeType.cast(instance);
  }
}
