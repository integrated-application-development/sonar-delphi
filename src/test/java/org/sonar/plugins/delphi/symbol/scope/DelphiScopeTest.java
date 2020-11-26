package org.sonar.plugins.delphi.symbol.scope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.delphi.symbol.scope.DelphiScope.unknownScope;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;

class DelphiScopeTest {
  private DelphiScope scope;
  private VariableNameDeclaration declaration;
  private DelphiNameOccurrence occurrence;

  @BeforeEach
  void setup() {
    DelphiNode location = mock(DelphiNode.class);
    when(location.getScope()).thenReturn(unknownScope());

    scope = new LocalScope();
    declaration = VariableNameDeclaration.compilerVariable("Image", unknownType(), unknownScope());
    occurrence = new DelphiNameOccurrence(location, "image");
    occurrence.setNameDeclaration(declaration);
    scope.addDeclaration(declaration);
  }

  @Test
  void testContains() {
    assertThat(scope.contains(occurrence)).isTrue();
  }

  @Test
  void testGetDeclarations() {
    assertThat(scope.getDeclarations()).containsKey(declaration);
  }

  @Test
  void testGetDeclarationsByClass() {
    assertThat(scope.getDeclarations(VariableNameDeclaration.class)).containsKey(declaration);
  }

  @Test
  void testDuplicateNameDeclarations() {
    assertThatThrownBy(() -> scope.addDeclaration(declaration))
        .isInstanceOf(RuntimeException.class);
  }
}
