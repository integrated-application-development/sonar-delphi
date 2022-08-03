package org.sonar.plugins.delphi.symbol.declaration;

import static org.assertj.core.api.Assertions.assertThat;

import org.antlr.runtime.CommonToken;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.node.CommonDelphiNode;
import org.sonar.plugins.delphi.type.Type.TypeParameterType;
import org.sonar.plugins.delphi.type.generic.DelphiTypeParameterType;

class TypeParameterNameDeclarationTest {
  @Test
  void testEquals() {
    TypeParameterType fooType = DelphiTypeParameterType.create("Foo");

    TypeParameterNameDeclaration foo = createTypeParameter(fooType);
    TypeParameterNameDeclaration otherFoo = createTypeParameter(fooType);
    TypeParameterNameDeclaration fooWithDifferentTypeInstance = createTypeParameter("Foo");
    TypeParameterNameDeclaration bar = createTypeParameter("Bar");

    assertThat(foo)
        .isEqualTo(foo)
        .isNotEqualTo(null)
        .isNotEqualTo(new Object())
        .isEqualTo(otherFoo)
        .isEqualByComparingTo(otherFoo)
        .hasSameHashCodeAs(otherFoo)
        .isNotEqualTo(fooWithDifferentTypeInstance)
        .doesNotHaveSameHashCodeAs(fooWithDifferentTypeInstance)
        .isNotEqualTo(bar)
        .isNotEqualByComparingTo(bar)
        .doesNotHaveSameHashCodeAs(bar);
  }

  @Test
  void testToString() {
    assertThat(createTypeParameter("T")).hasToString("type parameter <T>");
  }

  private static TypeParameterNameDeclaration createTypeParameter(String name) {
    return createTypeParameter(DelphiTypeParameterType.create(name));
  }

  private static TypeParameterNameDeclaration createTypeParameter(TypeParameterType type) {
    return new TypeParameterNameDeclaration(
        new CommonDelphiNode(new CommonToken(DelphiLexer.TkNameDeclaration, type.getImage())),
        type);
  }
}
