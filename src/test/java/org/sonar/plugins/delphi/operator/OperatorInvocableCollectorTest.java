package org.sonar.plugins.delphi.operator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.type.ArrayOption;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.factory.TypeFactory;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicType;
import org.sonar.plugins.delphi.utils.types.TypeFactoryUtils;

class OperatorInvocableCollectorTest {
  @Test
  void testUnhandledOperatorShouldThrow() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThatThrownBy(() -> collector.collect(DelphiType.unknownType(), mock(Operator.class)))
        .withFailMessage("Unhandled Operator")
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void testPointerMathTypeWithInvalidOperatorShouldNotCollectPointerMathOperators() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThat(
            collector.collect(typeFactory.getIntrinsic(IntrinsicType.PCHAR), BinaryOperator.DIVIDE))
        .hasSize(1);
  }

  @Test
  void testVariantShouldCollectInOperator() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThat(
            collector.collect(typeFactory.getIntrinsic(IntrinsicType.VARIANT), BinaryOperator.IN))
        .hasSize(1);
  }

  @Test
  void testSetShouldCollectAddOperators() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThat(collector.collect(typeFactory.emptySet(), BinaryOperator.ADD)).hasSize(9);
  }

  @Test
  void testVariantShouldNotCollectAsOperator() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThat(
            collector.collect(typeFactory.getIntrinsic(IntrinsicType.VARIANT), BinaryOperator.AS))
        .isEmpty();
  }

  @Test
  void testDynamicArrayShouldNotCollectAsOperator() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThat(
            collector.collect(
                typeFactory.array(
                    "Foo",
                    typeFactory.getIntrinsic(IntrinsicType.STRING),
                    Set.of(ArrayOption.DYNAMIC)),
                BinaryOperator.AS))
        .isEmpty();
  }

  @Test
  void testNothingShouldCollectAddressOperator() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThat(collector.collect(typeFactory.untypedType(), UnaryOperator.ADDRESS)).isEmpty();
  }
}
