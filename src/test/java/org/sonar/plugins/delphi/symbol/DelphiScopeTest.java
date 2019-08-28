package org.sonar.plugins.delphi.symbol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.sonar.plugins.delphi.symbol.UnknownScope.unknownScope;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;

public class DelphiScopeTest {
  private DelphiScope scope;
  private DelphiNameDeclaration declaration;
  private DelphiNameOccurrence occurrence;

  @Before
  public void setup() {
    scope = new LocalScope();
    declaration = VariableNameDeclaration.compilerVariable("Image", unknownType(), unknownScope());
    occurrence = new DelphiNameOccurrence(mock(DelphiNode.class), "image");
    occurrence.setNameDeclaration(declaration);
  }

  @Test
  public void testContains() {
    scope.addDeclaration(declaration);
    assertThat(scope.contains(occurrence)).isTrue();
  }

  @Test
  public void testDuplicateNameDeclarations() {
    scope.addDeclaration(declaration);
    assertThatThrownBy(() -> scope.addDeclaration(declaration))
        .isInstanceOf(RuntimeException.class);
  }
}
