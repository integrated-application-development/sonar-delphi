package org.sonar.plugins.delphi.symbol.scope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.antlr.ast.node.Visibility.VisibilityType;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.declaration.MethodKind;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;

class MethodScopeTest {
  @Test
  void testToString() {
    MethodScope methodScope = new MethodScope();
    assertThat(methodScope).hasToString("<MethodScope>");

    SymbolicNode symbolicNode = SymbolicNode.imaginary("Foo", DelphiScope.unknownScope());
    methodScope.setMethodNameDeclaration(
        new MethodNameDeclaration(
            symbolicNode,
            "Unit.Foo",
            DelphiType.unknownType(),
            Collections.emptySet(),
            false,
            true,
            MethodKind.FUNCTION,
            mock(ProceduralType.class),
            null,
            VisibilityType.PUBLIC,
            Collections.emptyList()));

    assertThat(methodScope).hasToString("Foo <MethodScope>");
  }
}
