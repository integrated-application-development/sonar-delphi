package org.sonar.plugins.delphi.symbol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;

public class DeclarationScopeTest {
  @Test
  public void testFindDeclaration() {
    DeclarationScope scope = new DeclarationScope();
    VariableNameDeclaration declaration = mock(VariableNameDeclaration.class);
    when(declaration.getImage()).thenReturn("Image");
    scope.addDeclaration(declaration);
    DelphiNameOccurrence occurrence = new DelphiNameOccurrence(mock(DelphiNode.class), "Image");
    assertThat(scope.findDeclaration(occurrence)).isEmpty();
  }
}
