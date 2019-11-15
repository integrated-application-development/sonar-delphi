package org.sonar.plugins.delphi.antlr.ast;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public class DelphiNodeTest {
  private static final String MISSING_MODIFIERS = "Expected abstract or final modifier in %s";
  private static final String ABSTRACT_ACCEPT_IMPLEMENTED =
      "Expected accept not to be implemented in abstract nodes %s";

  @Test
  public void testAllNodesShouldBeAbstractOrFinal() {
    Set<String> failed =
        DelphiNodeUtils.getNodeTypes().stream()
            .filter(not(DelphiNodeUtils::hasExpectedModifiers))
            .map(Class::getSimpleName)
            .collect(Collectors.toSet());

    assertThat(failed).as(String.format(MISSING_MODIFIERS, failed)).isEmpty();
  }

  @Test
  public void testAbstractNodesShouldNotImplementAccept() {
    Set<String> failed =
        DelphiNodeUtils.getNodeTypes().stream()
            .filter(type -> Modifier.isAbstract(type.getModifiers()))
            .filter(DelphiNodeUtils::implementsAccept)
            .map(Class::getSimpleName)
            .collect(Collectors.toSet());

    assertThat(failed).as(String.format(ABSTRACT_ACCEPT_IMPLEMENTED, failed)).isEmpty();
  }

  @Test
  public void testAcceptShouldCallCorrectVisitorMethod() {
    for (DelphiNode node : DelphiNodeUtils.getNodeInstances()) {
      DelphiParserVisitor<?> visitor = spy(new DelphiParserVisitor() {});
      node.accept(visitor, null);
      verify(visitor, atLeastOnce()).visit(any(node.getClass()), any());
    }
  }

  @Test
  public void testAllNodesCanBeInstantiated() {
    DelphiNodeUtils.getNodeInstances();
  }
}
