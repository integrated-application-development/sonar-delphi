package org.sonar.plugins.delphi.symbol.scope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.type.Type;

class TypeScopeTest {
  @Test
  void testToString() {
    TypeScope scope = new TypeScope();

    Type type = mock(Type.class);
    when(type.getImage()).thenReturn("Foo");
    scope.setType(type);

    assertThat(scope).hasToString("Foo <TypeScope>");
  }
}
