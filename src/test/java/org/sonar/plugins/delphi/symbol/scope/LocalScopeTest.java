package org.sonar.plugins.delphi.symbol.scope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.delphi.type.DelphiType;

class LocalScopeTest {
  @Test
  void testOnlyVariableDeclarationsAllowed() {
    LocalScope scope = new LocalScope();

    VariableNameDeclaration variable =
        VariableNameDeclaration.compilerVariable(
            "Foo", DelphiType.unknownType(), DelphiScope.unknownScope());

    scope.addDeclaration(variable);

    SymbolicNode symbolicNode = SymbolicNode.imaginary("Bar", DelphiScope.unknownScope());
    TypeNameDeclaration type =
        new TypeNameDeclaration(symbolicNode, DelphiType.unknownType(), "Bar");

    assertThatThrownBy(() -> scope.addDeclaration(type))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void testToString() {
    LocalScope scope = new LocalScope();
    assertThat(scope).hasToString("<LocalScope>");
  }
}
