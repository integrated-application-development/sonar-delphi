package org.sonar.plugins.delphi.antlr.ast;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.sonar.plugins.delphi.antlr.ast.DelphiNodeUtils.ARE_DELPHI_NODES;
import static org.sonar.plugins.delphi.antlr.ast.DelphiNodeUtils.HAVE_TOKEN_CONSTRUCTOR;
import static org.sonar.plugins.delphi.antlr.ast.DelphiNodeUtils.IMPLEMENT_ACCEPT;
import static org.sonar.plugins.delphi.antlr.ast.DelphiNodeUtils.NODE_PACKAGE;

import com.tngtech.archunit.core.domain.JavaModifier;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public class DelphiNodeTest {
  @Test
  public void testAllNodesShouldBeAbstractOrFinal() {
    classes()
        .that(ARE_DELPHI_NODES)
        .should()
        .haveModifier(JavaModifier.ABSTRACT)
        .orShould()
        .haveModifier(JavaModifier.FINAL)
        .check(NODE_PACKAGE);
  }

  @Test
  public void testAllNodesShouldEndNameWithNode() {
    classes().that(ARE_DELPHI_NODES).should().haveSimpleNameEndingWith("Node").check(NODE_PACKAGE);
  }

  @Test
  public void testAbstractNodesShouldNotImplementAccept() {
    classes()
        .that(ARE_DELPHI_NODES)
        .and(IMPLEMENT_ACCEPT)
        .should()
        .notHaveModifier(JavaModifier.ABSTRACT)
        .check(NODE_PACKAGE);
  }

  @Test
  public void testAcceptShouldCallCorrectVisitorMethod() {
    for (DelphiNode node : DelphiNodeUtils.getNodeInstances()) {
      DelphiParserVisitor<?> visitor = spy(new DelphiParserVisitor<>() {});
      node.accept(visitor, null);
      verify(visitor, atLeastOnce()).visit(any(node.getClass()), any());
    }
  }

  @Test
  public void testAllNodesHaveTokenConstructor() {
    classes()
        .that(ARE_DELPHI_NODES)
        .should(HAVE_TOKEN_CONSTRUCTOR)
        .because("this is needed for reflective instantiation")
        .check(NODE_PACKAGE);
  }
}
