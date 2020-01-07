package org.sonar.plugins.delphi.symbol.scope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration.compilerVariable;
import static org.sonar.plugins.delphi.symbol.scope.UnknownScope.unknownScope;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;

import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;

public class DeclarationScopeTest {
  @Test
  public void testFindDeclarationShouldAlwaysReturnEmpty() {
    DeclarationScope scope = new DeclarationScope();
    VariableNameDeclaration declaration = compilerVariable("Image", unknownType(), unknownScope());
    scope.addDeclaration(declaration);
    DelphiNameOccurrence occurrence = new DelphiNameOccurrence(mock(DelphiNode.class), "Image");
    assertThat(scope.getDeclarations()).containsKey(declaration);
    assertThat(scope.findDeclaration(occurrence)).isEmpty();
  }
}
