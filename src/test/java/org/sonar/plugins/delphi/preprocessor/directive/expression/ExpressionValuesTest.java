package org.sonar.plugins.delphi.preprocessor.directive.expression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionValues.createBoolean;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionValues.createDecimal;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionValues.createInteger;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionValues.createSet;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionValues.createString;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionValues.unknownValue;

import java.util.Collections;
import java.util.Set;
import org.junit.Test;
import org.sonar.plugins.delphi.preprocessor.directive.CompilerDirective.Expression.ConstExpressionType;
import org.sonar.plugins.delphi.preprocessor.directive.CompilerDirective.Expression.ExpressionValue;

public class ExpressionValuesTest {

  @Test
  public void testStringValue() {
    ExpressionValue value = createString("string");
    assertThat(value.type()).isEqualTo(ConstExpressionType.STRING);
    assertThat(value.asString()).isEqualTo("string");
    assertThat(value.asInteger()).isEqualTo(0);
    assertThat(value.asDecimal()).isEqualTo(0.0);
    assertThat(value.asBoolean()).isFalse();
    assertThat(value.asSet()).isEmpty();

    assertThat(value)
        .isNotEqualTo(null)
        .isNotEqualTo(createString("other"))
        .isNotEqualTo(unknownValue())
        .isEqualTo(value)
        .isEqualTo(createString("string"))
        .hasSameHashCodeAs(createString("string"));
  }

  @Test
  public void testIntegerValue() {
    ExpressionValue value = createInteger(1);
    assertThat(value.type()).isEqualTo(ConstExpressionType.INTEGER);
    assertThat(value.asString()).isEmpty();
    assertThat(value.asInteger()).isEqualTo(1);
    assertThat(value.asDecimal()).isEqualTo(1.0);
    assertThat(value.asBoolean()).isFalse();
    assertThat(value.asSet()).isEmpty();

    assertThat(value)
        .isNotEqualTo(null)
        .isNotEqualTo(createInteger(2))
        .isNotEqualTo(unknownValue())
        .isEqualTo(value)
        .isEqualTo(createInteger(1))
        .hasSameHashCodeAs(createInteger(1));
  }

  @Test
  public void testDecimalValue() {
    ExpressionValue value = createDecimal(1.0);
    assertThat(value.type()).isEqualTo(ConstExpressionType.DECIMAL);
    assertThat(value.asString()).isEmpty();
    assertThat(value.asInteger()).isEqualTo(1);
    assertThat(value.asDecimal()).isEqualTo(1.0);
    assertThat(value.asBoolean()).isFalse();
    assertThat(value.asSet()).isEmpty();

    assertThat(value)
        .isNotEqualTo(null)
        .isNotEqualTo(createDecimal(2.0))
        .isNotEqualTo(unknownValue())
        .isEqualTo(value)
        .isEqualTo(createDecimal(1.0))
        .hasSameHashCodeAs(createDecimal(1.0));
  }

  @Test
  public void testBooleanValue() {
    ExpressionValue value = createBoolean(true);
    assertThat(value.type()).isEqualTo(ConstExpressionType.BOOLEAN);
    assertThat(value.asString()).isEmpty();
    assertThat(value.asInteger()).isEqualTo(0);
    assertThat(value.asDecimal()).isEqualTo(0.0);
    assertThat(value.asBoolean()).isTrue();
    assertThat(value.asSet()).isEmpty();

    assertThat(value)
        .isNotEqualTo(null)
        .isNotEqualTo(createBoolean(false))
        .isNotEqualTo(unknownValue())
        .isEqualTo(value)
        .isEqualTo(createBoolean(true))
        .hasSameHashCodeAs(createBoolean(true));
  }

  @Test
  public void testSetValue() {
    ExpressionValue value = createSet(Set.of(createInteger(1), createInteger(2), createInteger(3)));
    assertThat(value.type()).isEqualTo(ConstExpressionType.SET);
    assertThat(value.asString()).isEmpty();
    assertThat(value.asInteger()).isEqualTo(0);
    assertThat(value.asDecimal()).isEqualTo(0.0);
    assertThat(value.asBoolean()).isFalse();
    assertThat(value.asSet()).hasSize(3);

    ExpressionValue other = createSet(Set.of(createInteger(1), createInteger(2), createInteger(3)));

    assertThat(value)
        .isNotEqualTo(null)
        .isNotEqualTo(createInteger(123))
        .isNotEqualTo(createSet(Collections.emptySet()))
        .isEqualTo(value)
        .isEqualTo(other)
        .hasSameHashCodeAs(other);
  }

  @Test
  public void testUnknownValue() {
    ExpressionValue value = unknownValue();
    assertThat(value.type()).isEqualTo(ConstExpressionType.UNKNOWN);
    assertThat(value.asString()).isEmpty();
    assertThat(value.asInteger()).isEqualTo(0);
    assertThat(value.asDecimal()).isEqualTo(0.0);
    assertThat(value.asBoolean()).isFalse();
    assertThat(value.asSet()).isEmpty();
  }
}
