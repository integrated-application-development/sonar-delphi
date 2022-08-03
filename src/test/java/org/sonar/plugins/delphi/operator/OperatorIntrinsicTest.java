package org.sonar.plugins.delphi.operator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.parameter.IntrinsicParameter;

class OperatorIntrinsicTest {
  private static final OperatorIntrinsic INTRINSIC =
      new OperatorIntrinsic("Foo", List.of(DelphiType.untypedType()), DelphiType.voidType());

  @Test
  void testGetName() {
    assertThat(INTRINSIC.getName()).isEqualTo("Foo");
  }

  @Test
  void testGetParameters() {
    assertThat(INTRINSIC.getParameters())
        .isEqualTo(List.of(IntrinsicParameter.create(DelphiType.untypedType())));
  }

  @Test
  void testGetReturnType() {
    assertThat(INTRINSIC.getReturnType()).isEqualTo(DelphiType.voidType());
  }

  @Test
  void testIsCallable() {
    assertThat(INTRINSIC.isCallable()).isFalse();
  }

  @Test
  void testIsClassInvocable() {
    assertThat(INTRINSIC.isClassInvocable()).isTrue();
  }

  @Test
  void testEquals() {
    OperatorIntrinsic equal =
        new OperatorIntrinsic("Foo", List.of(DelphiType.untypedType()), DelphiType.voidType());
    OperatorIntrinsic differentName =
        new OperatorIntrinsic("Bar", List.of(DelphiType.untypedType()), DelphiType.voidType());
    OperatorIntrinsic differentParameters =
        new OperatorIntrinsic("Foo", Collections.emptyList(), DelphiType.voidType());
    OperatorIntrinsic differentReturnType =
        new OperatorIntrinsic("Foo", List.of(DelphiType.untypedType()), DelphiType.untypedType());

    assertThat(INTRINSIC)
        .isEqualTo(INTRINSIC)
        .isNotEqualTo(null)
        .isNotEqualTo(new Object())
        .isEqualTo(equal)
        .hasSameHashCodeAs(equal)
        .isNotEqualTo(differentName)
        .doesNotHaveSameHashCodeAs(differentName)
        .isNotEqualTo(differentParameters)
        .doesNotHaveSameHashCodeAs(differentParameters)
        .isNotEqualTo(differentReturnType)
        .doesNotHaveSameHashCodeAs(differentReturnType);
  }
}
