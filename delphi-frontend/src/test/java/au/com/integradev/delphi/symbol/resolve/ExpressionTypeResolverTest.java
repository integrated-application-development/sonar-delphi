/*
 * Sonar Delphi Plugin
 * Copyright (C) 2026 Integrated Application Development
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
package au.com.integradev.delphi.symbol.resolve;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.communitydelphi.api.type.StructKind.CLASS;
import static org.sonar.plugins.communitydelphi.api.type.TypeFactory.unknownType;

import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import au.com.integradev.delphi.utils.types.TypeMocker;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.IfExpressionNode;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class ExpressionTypeResolverTest {
  private static final TypeFactory FACTORY = TypeFactoryUtils.defaultFactory();
  private static final ExpressionTypeResolver RESOLVER = new ExpressionTypeResolver(FACTORY);

  @Test
  void testIfExpressionWithSameTypeOnBothBranchesReturnsThatType() {
    Type integer = FACTORY.getIntrinsic(IntrinsicType.INTEGER);
    assertThat(resolve(integer, integer).is(integer)).isTrue();
  }

  @Test
  void testIfExpressionWithUnknownThenBranchReturnsElseType() {
    Type integer = FACTORY.getIntrinsic(IntrinsicType.INTEGER);
    assertThat(resolve(unknownType(), integer).is(integer)).isTrue();
  }

  @Test
  void testIfExpressionWithUnknownElseBranchReturnsThenType() {
    Type integer = FACTORY.getIntrinsic(IntrinsicType.INTEGER);
    assertThat(resolve(integer, unknownType()).is(integer)).isTrue();
  }

  @Test
  void testIfExpressionPromotesIntegerToReal() {
    Type integer = FACTORY.getIntrinsic(IntrinsicType.INTEGER);
    Type real = FACTORY.getIntrinsic(IntrinsicType.DOUBLE);
    assertThat(resolve(integer, real).is(real)).isTrue();
    assertThat(resolve(real, integer).is(real)).isTrue();
  }

  @Test
  void testIfExpressionWidensSmallerIntegerToLarger() {
    Type byteType = FACTORY.getIntrinsic(IntrinsicType.BYTE);
    Type integer = FACTORY.getIntrinsic(IntrinsicType.INTEGER);
    assertThat(resolve(byteType, integer).is(integer)).isTrue();
    assertThat(resolve(integer, byteType).is(integer)).isTrue();
  }

  @Test
  void testIfExpressionWithDescendantAndAncestorClassReturnsAncestor() {
    Type base = TypeMocker.struct("TBase", CLASS);
    Type derived = TypeMocker.struct("TDerived", CLASS, base);
    assertThat(resolve(derived, base).is(base)).isTrue();
    assertThat(resolve(base, derived).is(base)).isTrue();
  }

  @Test
  void testIfExpressionWithSiblingClassesReturnsCommonAncestor() {
    Type base = TypeMocker.struct("TBase", CLASS);
    Type left = TypeMocker.struct("TLeft", CLASS, base);
    Type right = TypeMocker.struct("TRight", CLASS, base);
    assertThat(resolve(left, right).is(base)).isTrue();
  }

  @Test
  void testIfExpressionWithUnrelatedTypesReturnsUnknown() {
    Type record = TypeMocker.struct("TFoo", CLASS);
    Type integer = FACTORY.getIntrinsic(IntrinsicType.INTEGER);
    assertThat(resolve(record, integer).isUnknown()).isTrue();
  }

  private static Type resolve(Type thenType, Type elseType) {
    IfExpressionNode node = mock(IfExpressionNode.class);
    ExpressionNode thenExpression = mock(ExpressionNode.class);
    ExpressionNode elseExpression = mock(ExpressionNode.class);
    when(thenExpression.getType()).thenReturn(thenType);
    when(elseExpression.getType()).thenReturn(elseType);
    when(node.getThenExpression()).thenReturn(thenExpression);
    when(node.getElseExpression()).thenReturn(elseExpression);
    return RESOLVER.resolve(node);
  }
}
