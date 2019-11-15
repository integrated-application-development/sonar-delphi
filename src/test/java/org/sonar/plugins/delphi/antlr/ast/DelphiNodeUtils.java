package org.sonar.plugins.delphi.antlr.ast;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.antlr.runtime.Token;
import org.reflections.Reflections;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class DelphiNodeUtils {
  private static final Class<?>[] ACCEPT_PARAMS = {DelphiParserVisitor.class, Object.class};

  private static final String NODE_INSTANTIATION_ERROR =
      "Could not instantiate %s. Requires public constructor(Token)";

  private DelphiNodeUtils() {
    // Utility class
  }

  public static Set<Class<? extends DelphiNode>> getNodeTypes() {
    Reflections reflections = new Reflections("org.sonar.plugins.delphi.antlr.ast.node");
    Set<Class<? extends DelphiNode>> nodeTypes = reflections.getSubTypesOf(DelphiNode.class);
    nodeTypes.add(DelphiNode.class);
    return nodeTypes;
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
          .as(String.format(NODE_INSTANTIATION_ERROR, nodeType.getSimpleName()))
          .isNotNull();
    } catch (Exception e) {
      throw new AssertionError(e);
    }

    return nodeType.cast(instance);
  }

  public static boolean hasExpectedModifiers(Class<? extends DelphiNode> clazz) {
    return Modifier.isAbstract(clazz.getModifiers()) || Modifier.isFinal(clazz.getModifiers());
  }

  public static boolean shouldImplementAccept(Class<? extends DelphiNode> clazz) {
    return Modifier.isFinal(clazz.getModifiers());
  }

  public static boolean implementsAccept(Class<? extends DelphiNode> clazz) {
    return Arrays.stream(clazz.getMethods())
        .filter(method -> !Modifier.isAbstract(method.getModifiers()))
        .anyMatch(DelphiNodeUtils::isAcceptMethod);
  }

  private static boolean isAcceptMethod(Method method) {
    return method.getName().equals("accept")
        && Arrays.equals(method.getParameterTypes(), ACCEPT_PARAMS);
  }
}
