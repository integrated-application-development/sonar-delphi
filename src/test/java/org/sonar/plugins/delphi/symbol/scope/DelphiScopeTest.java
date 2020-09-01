package org.sonar.plugins.delphi.symbol.scope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.delphi.symbol.scope.DelphiScope.unknownScope;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;

public class DelphiScopeTest {
  private DelphiScope scope;
  private VariableNameDeclaration declaration;
  private DelphiNameOccurrence occurrence;

  @Before
  public void setup() {
    DelphiNode location = mock(DelphiNode.class);
    when(location.getScope()).thenReturn(unknownScope());

    scope = new LocalScope();
    declaration = VariableNameDeclaration.compilerVariable("Image", unknownType(), unknownScope());
    occurrence = new DelphiNameOccurrence(location, "image");
    occurrence.setNameDeclaration(declaration);
    scope.addDeclaration(declaration);
  }

  @Test
  public void testContains() {
    assertThat(scope.contains(occurrence)).isTrue();
  }

  @Test
  public void testGetDeclarations() {
    assertThat(scope.getDeclarations()).containsKey(declaration);
  }

  @Test
  public void testGetDeclarationsByClass() {
    assertThat(scope.getDeclarations(VariableNameDeclaration.class)).containsKey(declaration);
  }

  @Test
  public void testDuplicateNameDeclarations() {
    assertThatThrownBy(() -> scope.addDeclaration(declaration))
        .isInstanceOf(RuntimeException.class);
  }
}
