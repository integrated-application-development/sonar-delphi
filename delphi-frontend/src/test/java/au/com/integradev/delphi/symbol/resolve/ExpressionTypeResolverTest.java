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

/**
 * The expected result type of every {@code if ... then ... else ...} expression in this test was
 * obtained from the Delphi 13 (compiler version 37.0) {@code dcc64} compiler, by assigning the
 * expression to an incompatible variable and reading the type named in the resulting {@code E2010
 * Incompatible types} error.
 */
class ExpressionTypeResolverTest {
  private static final TypeFactory FACTORY = TypeFactoryUtils.defaultFactory();
  private static final ExpressionTypeResolver RESOLVER = new ExpressionTypeResolver(FACTORY);

  private static Type intrinsic(IntrinsicType type) {
    return FACTORY.getIntrinsic(type);
  }

  // ---- same type / unknown -------------------------------------------------

  @Test
  void testSameTypeOnBothBranchesReturnsThatType() {
    assertResolvesBoth(IntrinsicType.INTEGER, IntrinsicType.INTEGER, IntrinsicType.INTEGER);
    assertResolvesBoth(IntrinsicType.CARDINAL, IntrinsicType.CARDINAL, IntrinsicType.CARDINAL);
    assertResolvesBoth(IntrinsicType.UINT64, IntrinsicType.UINT64, IntrinsicType.UINT64);
    assertResolvesBoth(IntrinsicType.CURRENCY, IntrinsicType.CURRENCY, IntrinsicType.CURRENCY);
    assertResolvesBoth(IntrinsicType.COMP, IntrinsicType.COMP, IntrinsicType.COMP);
  }

  @Test
  void testUnknownThenBranchReturnsElseType() {
    Type integer = intrinsic(IntrinsicType.INTEGER);
    assertThat(resolve(unknownType(), integer).is(integer)).isTrue();
  }

  @Test
  void testUnknownElseBranchReturnsThenType() {
    Type integer = intrinsic(IntrinsicType.INTEGER);
    assertThat(resolve(integer, unknownType()).is(integer)).isTrue();
  }

  // ---- integer widening ----------------------------------------------------

  @Test
  void testTwoSignedIntegersWidenToSignedAtLeastInteger() {
    assertResolvesBoth(IntrinsicType.SHORTINT, IntrinsicType.SMALLINT, IntrinsicType.INTEGER);
    assertResolvesBoth(IntrinsicType.SHORTINT, IntrinsicType.INTEGER, IntrinsicType.INTEGER);
    assertResolvesBoth(IntrinsicType.SMALLINT, IntrinsicType.INTEGER, IntrinsicType.INTEGER);
    assertResolvesBoth(IntrinsicType.INTEGER, IntrinsicType.INT64, IntrinsicType.INT64);
    assertResolvesBoth(IntrinsicType.SHORTINT, IntrinsicType.INT64, IntrinsicType.INT64);
  }

  @Test
  void testTwoUnsignedIntegersWidenToSignedInteger() {
    assertResolvesBoth(IntrinsicType.BYTE, IntrinsicType.WORD, IntrinsicType.INTEGER);
    assertResolvesBoth(IntrinsicType.BYTE, IntrinsicType.CARDINAL, IntrinsicType.INTEGER);
    assertResolvesBoth(IntrinsicType.WORD, IntrinsicType.CARDINAL, IntrinsicType.INTEGER);
  }

  @Test
  void testSmallSignedAndUnsignedWidenToInteger() {
    assertResolvesBoth(IntrinsicType.SHORTINT, IntrinsicType.BYTE, IntrinsicType.INTEGER);
    assertResolvesBoth(IntrinsicType.SHORTINT, IntrinsicType.WORD, IntrinsicType.INTEGER);
    assertResolvesBoth(IntrinsicType.SMALLINT, IntrinsicType.WORD, IntrinsicType.INTEGER);
    assertResolvesBoth(IntrinsicType.BYTE, IntrinsicType.INTEGER, IntrinsicType.INTEGER);
    assertResolvesBoth(IntrinsicType.WORD, IntrinsicType.INTEGER, IntrinsicType.INTEGER);
  }

  @Test
  void testSignedMixedWithCardinalWidenToInt64() {
    assertResolvesBoth(IntrinsicType.SHORTINT, IntrinsicType.CARDINAL, IntrinsicType.INT64);
    assertResolvesBoth(IntrinsicType.SMALLINT, IntrinsicType.CARDINAL, IntrinsicType.INT64);
    assertResolvesBoth(IntrinsicType.INTEGER, IntrinsicType.CARDINAL, IntrinsicType.INT64);
  }

  @Test
  void testAnythingMixedWith64BitWidensToInt64() {
    assertResolvesBoth(IntrinsicType.CARDINAL, IntrinsicType.INT64, IntrinsicType.INT64);
    assertResolvesBoth(IntrinsicType.CARDINAL, IntrinsicType.UINT64, IntrinsicType.INT64);
    assertResolvesBoth(IntrinsicType.INTEGER, IntrinsicType.UINT64, IntrinsicType.INT64);
    assertResolvesBoth(IntrinsicType.INT64, IntrinsicType.UINT64, IntrinsicType.INT64);
    assertResolvesBoth(IntrinsicType.WORD, IntrinsicType.INT64, IntrinsicType.INT64);
  }

  // ---- real promotion ------------------------------------------------------

  @Test
  void testTwoRealsCollapseToDouble() {
    assertResolvesBoth(IntrinsicType.SINGLE, IntrinsicType.DOUBLE, IntrinsicType.DOUBLE);
    assertResolvesBoth(IntrinsicType.SINGLE, IntrinsicType.EXTENDED, IntrinsicType.DOUBLE);
    assertResolvesBoth(IntrinsicType.DOUBLE, IntrinsicType.EXTENDED, IntrinsicType.DOUBLE);
    assertResolvesBoth(IntrinsicType.SINGLE, IntrinsicType.CURRENCY, IntrinsicType.DOUBLE);
    assertResolvesBoth(IntrinsicType.DOUBLE, IntrinsicType.CURRENCY, IntrinsicType.DOUBLE);
    assertResolvesBoth(IntrinsicType.EXTENDED, IntrinsicType.CURRENCY, IntrinsicType.DOUBLE);
    assertResolvesBoth(IntrinsicType.SINGLE, IntrinsicType.COMP, IntrinsicType.DOUBLE);
    assertResolvesBoth(IntrinsicType.DOUBLE, IntrinsicType.COMP, IntrinsicType.DOUBLE);
    assertResolvesBoth(IntrinsicType.EXTENDED, IntrinsicType.COMP, IntrinsicType.DOUBLE);
  }

  @Test
  void testCurrencyAndCompCollapseToComp() {
    assertResolvesBoth(IntrinsicType.CURRENCY, IntrinsicType.COMP, IntrinsicType.COMP);
  }

  // ---- integer mixed with real ---------------------------------------------

  @Test
  void testIntegerMixedWithFloatingRealPromotesToDouble() {
    assertResolvesBoth(IntrinsicType.INTEGER, IntrinsicType.SINGLE, IntrinsicType.DOUBLE);
    assertResolvesBoth(IntrinsicType.INTEGER, IntrinsicType.DOUBLE, IntrinsicType.DOUBLE);
    assertResolvesBoth(IntrinsicType.INTEGER, IntrinsicType.EXTENDED, IntrinsicType.DOUBLE);
    assertResolvesBoth(IntrinsicType.INT64, IntrinsicType.DOUBLE, IntrinsicType.DOUBLE);
  }

  @Test
  void testIntegerMixedWithCurrencyOrCompPromotesToComp() {
    assertResolvesBoth(IntrinsicType.INTEGER, IntrinsicType.CURRENCY, IntrinsicType.COMP);
    assertResolvesBoth(IntrinsicType.INTEGER, IntrinsicType.COMP, IntrinsicType.COMP);
    assertResolvesBoth(IntrinsicType.BYTE, IntrinsicType.CURRENCY, IntrinsicType.COMP);
    assertResolvesBoth(IntrinsicType.INT64, IntrinsicType.COMP, IntrinsicType.COMP);
  }

  // ---- characters and strings ----------------------------------------------

  @Test
  void testCharMixedWithUnicodeStringPromotesToString() {
    assertResolvesBoth(
        IntrinsicType.CHAR, IntrinsicType.UNICODESTRING, IntrinsicType.UNICODESTRING);
  }

  @Test
  void testStringMixedWithOtherStringPromotesToUnicodeString() {
    assertResolvesBoth(
        IntrinsicType.UNICODESTRING, IntrinsicType.ANSISTRING, IntrinsicType.UNICODESTRING);
    assertResolvesBoth(
        IntrinsicType.UNICODESTRING, IntrinsicType.SHORTSTRING, IntrinsicType.UNICODESTRING);
  }

  // ---- class types ---------------------------------------------------------

  @Test
  void testDescendantAndAncestorClassReturnsAncestor() {
    Type base = TypeMocker.struct("TBase", CLASS);
    Type derived = TypeMocker.struct("TDerived", CLASS, base);
    assertThat(resolve(derived, base).is(base)).isTrue();
    assertThat(resolve(base, derived).is(base)).isTrue();
  }

  @Test
  void testSiblingClassesReturnsCommonAncestor() {
    Type base = TypeMocker.struct("TBase", CLASS);
    Type left = TypeMocker.struct("TLeft", CLASS, base);
    Type right = TypeMocker.struct("TRight", CLASS, base);
    assertThat(resolve(left, right).is(base)).isTrue();
    assertThat(resolve(right, left).is(base)).isTrue();
  }

  // ---- incompatible branches (a compile error in Delphi) -------------------

  @Test
  void testUnrelatedTypesReturnUnknown() {
    Type fooClass = TypeMocker.struct("TFoo", CLASS);
    Type integer = intrinsic(IntrinsicType.INTEGER);
    assertThat(resolve(fooClass, integer).isUnknown()).isTrue();
    assertThat(resolve(integer, fooClass).isUnknown()).isTrue();
    assertThat(resolve(intrinsic(IntrinsicType.UNICODESTRING), integer).isUnknown()).isTrue();
  }

  private void assertResolvesBoth(
      IntrinsicType then, IntrinsicType elseType, IntrinsicType expect) {
    assertThat(resolve(intrinsic(then), intrinsic(elseType)).is(intrinsic(expect)))
        .as("%s | %s -> %s", then, elseType, expect)
        .isTrue();
    assertThat(resolve(intrinsic(elseType), intrinsic(then)).is(intrinsic(expect)))
        .as("%s | %s -> %s", elseType, then, expect)
        .isTrue();
  }

  private static Type resolve(Type thenType, Type elseType) {
    ExpressionNode thenExpression = mock(ExpressionNode.class);
    ExpressionNode elseExpression = mock(ExpressionNode.class);
    when(thenExpression.getType()).thenReturn(thenType);
    when(elseExpression.getType()).thenReturn(elseType);

    IfExpressionNode node = mock(IfExpressionNode.class);
    when(node.getThenExpression()).thenReturn(thenExpression);
    when(node.getElseExpression()).thenReturn(elseExpression);
    return RESOLVER.resolve(node);
  }
}
