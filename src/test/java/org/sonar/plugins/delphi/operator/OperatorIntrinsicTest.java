/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
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
