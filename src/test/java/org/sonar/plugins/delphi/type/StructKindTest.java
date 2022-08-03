package org.sonar.plugins.delphi.type;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;

class StructKindTest {
  @Test
  void testUnknownStructNodeShouldThrow() {
    assertThatThrownBy(() -> StructKind.fromNode(mock(DelphiNode.class)))
        .isInstanceOf(AssertionError.class);
  }
}
