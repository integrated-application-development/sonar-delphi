/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import au.com.integradev.delphi.antlr.DelphiLexer;
import au.com.integradev.delphi.antlr.ast.node.ArrayConstructorNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.CommonDelphiNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.DelphiNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.IntegerLiteralNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.ParenthesizedExpressionNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.PrimaryExpressionNodeImpl;
import au.com.integradev.delphi.type.factory.ArrayOption;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;
import org.antlr.runtime.CommonToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.sonar.plugins.communitydelphi.api.ast.CommonDelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType.ProceduralKind;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class TypeInferrerTest {
  private static final TypeFactoryImpl TYPE_FACTORY =
      (TypeFactoryImpl) TypeFactoryUtils.defaultFactory();

  static class ArrayConstructorArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(arrayConstructor(), dynamicArrayType(TypeFactory.voidType())),
          Arguments.of(
              arrayConstructor(integerLiteral("127")), dynamicArrayType(IntrinsicType.INTEGER)),
          Arguments.of(
              arrayConstructor(integerLiteral("127"), integerLiteral("128")),
              dynamicArrayType(IntrinsicType.INTEGER)),
          Arguments.of(
              arrayConstructor(integerLiteral("127"), integerLiteral("128"), integerLiteral("255")),
              dynamicArrayType(IntrinsicType.INTEGER)),
          Arguments.of(
              arrayConstructor(
                  integerLiteral("127"),
                  integerLiteral("128"),
                  integerLiteral("255"),
                  integerLiteral("256")),
              dynamicArrayType(IntrinsicType.INTEGER)),
          Arguments.of(
              arrayConstructor(
                  integerLiteral("127"),
                  integerLiteral("128"),
                  integerLiteral("255"),
                  integerLiteral("256"),
                  integerLiteral("32767")),
              dynamicArrayType(IntrinsicType.INTEGER)),
          Arguments.of(
              arrayConstructor(
                  integerLiteral("127"),
                  integerLiteral("128"),
                  integerLiteral("255"),
                  integerLiteral("256"),
                  integerLiteral("32767"),
                  integerLiteral("32768")),
              dynamicArrayType(IntrinsicType.INTEGER)),
          Arguments.of(
              arrayConstructor(
                  integerLiteral("127"),
                  integerLiteral("128"),
                  integerLiteral("255"),
                  integerLiteral("256"),
                  integerLiteral("32767"),
                  integerLiteral("32768"),
                  integerLiteral("2147483647")),
              dynamicArrayType(IntrinsicType.INTEGER)),
          Arguments.of(
              arrayConstructor(
                  integerLiteral("127"),
                  integerLiteral("128"),
                  integerLiteral("255"),
                  integerLiteral("256"),
                  integerLiteral("32767"),
                  integerLiteral("32768"),
                  integerLiteral("2147483647"),
                  integerLiteral("2147483648")),
              dynamicArrayType(IntrinsicType.CARDINAL)),
          Arguments.of(
              arrayConstructor(
                  integerLiteral("127"),
                  integerLiteral("128"),
                  integerLiteral("255"),
                  integerLiteral("256"),
                  integerLiteral("32767"),
                  integerLiteral("32768"),
                  integerLiteral("2147483647"),
                  integerLiteral("2147483648"),
                  integerLiteral("4294967295")),
              dynamicArrayType(IntrinsicType.CARDINAL)),
          Arguments.of(
              arrayConstructor(
                  integerLiteral("127"),
                  integerLiteral("128"),
                  integerLiteral("255"),
                  integerLiteral("256"),
                  integerLiteral("32767"),
                  integerLiteral("32768"),
                  integerLiteral("2147483647"),
                  integerLiteral("2147483648"),
                  integerLiteral("4294967295"),
                  integerLiteral("4294967296")),
              dynamicArrayType(IntrinsicType.INT64)),
          Arguments.of(
              arrayConstructor(
                  integerLiteral("127"),
                  integerLiteral("128"),
                  integerLiteral("255"),
                  integerLiteral("256"),
                  integerLiteral("32767"),
                  integerLiteral("32768"),
                  integerLiteral("2147483647"),
                  integerLiteral("2147483648"),
                  integerLiteral("4294967295"),
                  integerLiteral("4294967296"),
                  integerLiteral("9223372036854775807")),
              dynamicArrayType(IntrinsicType.INT64)),
          Arguments.of(
              arrayConstructor(
                  integerLiteral("127"),
                  integerLiteral("128"),
                  integerLiteral("255"),
                  integerLiteral("256"),
                  integerLiteral("32767"),
                  integerLiteral("32768"),
                  integerLiteral("2147483647"),
                  integerLiteral("2147483648"),
                  integerLiteral("4294967295"),
                  integerLiteral("4294967296"),
                  integerLiteral("9223372036854775807"),
                  integerLiteral("9223372036854775808")),
              dynamicArrayType(IntrinsicType.UINT64)));
    }
  }

  static class IntegerArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(integerLiteral("-2147483649"), intrinsicType(IntrinsicType.INT64)),
          Arguments.of(integerLiteral("-2147483648"), intrinsicType(IntrinsicType.INTEGER)),
          Arguments.of(integerLiteral("-32769"), intrinsicType(IntrinsicType.INTEGER)),
          Arguments.of(integerLiteral("-32768"), intrinsicType(IntrinsicType.INTEGER)),
          Arguments.of(integerLiteral("-129"), intrinsicType(IntrinsicType.INTEGER)),
          Arguments.of(integerLiteral("-128"), intrinsicType(IntrinsicType.INTEGER)),
          Arguments.of(integerLiteral("-127"), intrinsicType(IntrinsicType.INTEGER)),
          Arguments.of(integerLiteral("128"), intrinsicType(IntrinsicType.INTEGER)),
          Arguments.of(integerLiteral("255"), intrinsicType(IntrinsicType.INTEGER)),
          Arguments.of(integerLiteral("256"), intrinsicType(IntrinsicType.INTEGER)),
          Arguments.of(integerLiteral("32767"), intrinsicType(IntrinsicType.INTEGER)),
          Arguments.of(integerLiteral("32768"), intrinsicType(IntrinsicType.INTEGER)),
          Arguments.of(integerLiteral("65535"), intrinsicType(IntrinsicType.INTEGER)),
          Arguments.of(integerLiteral("65536"), intrinsicType(IntrinsicType.INTEGER)),
          Arguments.of(integerLiteral("2147483647"), intrinsicType(IntrinsicType.INTEGER)),
          Arguments.of(integerLiteral("2147483648"), intrinsicType(IntrinsicType.CARDINAL)),
          Arguments.of(integerLiteral("4294967295"), intrinsicType(IntrinsicType.CARDINAL)),
          Arguments.of(integerLiteral("4294967296"), intrinsicType(IntrinsicType.INT64)),
          Arguments.of(integerLiteral("9223372036854775807"), intrinsicType(IntrinsicType.INT64)),
          Arguments.of(integerLiteral("9223372036854775808"), intrinsicType(IntrinsicType.UINT64)));
    }
  }

  static class ProceduralArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      Type integer = intrinsicType(IntrinsicType.INTEGER);
      ExpressionNode voidProcedural = procedural(TypeFactory.voidType());

      return Stream.of(
          Arguments.of(procedural(integer), integer),
          Arguments.of(voidProcedural, voidProcedural.getType()));
    }
  }

  static class RealArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      Type extended = intrinsicType(IntrinsicType.EXTENDED);
      Type comp = intrinsicType(IntrinsicType.COMP);
      Type currency = intrinsicType(IntrinsicType.CURRENCY);

      return Stream.of(
          Arguments.of(genericExpression(IntrinsicType.DOUBLE), extended),
          Arguments.of(genericExpression(IntrinsicType.REAL48), extended),
          Arguments.of(genericExpression(extended), extended),
          Arguments.of(genericExpression(comp), comp),
          Arguments.of(genericExpression(currency), currency));
    }
  }

  @Test
  void testInferNull() {
    assertInferred(null, TypeFactory.unknownType());
  }

  @ParameterizedTest
  @ArgumentsSource(ArrayConstructorArgumentsProvider.class)
  @ArgumentsSource(IntegerArgumentsProvider.class)
  @ArgumentsSource(ProceduralArgumentsProvider.class)
  @ArgumentsSource(RealArgumentsProvider.class)
  void testInfer(ExpressionNode expression, Type elementType) {
    assertInferred(expression, elementType);
  }

  private static Type dynamicArrayType(IntrinsicType elementType) {
    return dynamicArrayType(intrinsicType(elementType));
  }

  private static Type dynamicArrayType(Type elementType) {
    return TYPE_FACTORY.array(null, elementType, Set.of(ArrayOption.DYNAMIC));
  }

  private static Type intrinsicType(IntrinsicType intrinsic) {
    return TYPE_FACTORY.getIntrinsic(intrinsic);
  }

  private static ExpressionNode arrayConstructor(ExpressionNode... elements) {
    var arrayConstructor = makeSpy(new ArrayConstructorNodeImpl(DelphiLexer.TkArrayConstructor));
    arrayConstructor.addChild(commonDelphiNode(DelphiLexer.SQUARE_BRACKET_LEFT, "["));
    for (ExpressionNode element : elements) {
      arrayConstructor.addChild(element);
    }
    arrayConstructor.addChild(commonDelphiNode(DelphiLexer.SQUARE_BRACKET_RIGHT, "]"));

    var primaryExpression = makeSpy(new PrimaryExpressionNodeImpl(DelphiLexer.TkPrimaryExpression));
    primaryExpression.addChild(arrayConstructor);

    return primaryExpression;
  }

  private static ExpressionNode integerLiteral(String image) {
    var integerLiteralToken = new CommonToken(DelphiLexer.TkIntNumber, image);
    var integerLiteral = makeSpy(new IntegerLiteralNodeImpl(integerLiteralToken));

    var expression = makeSpy(new PrimaryExpressionNodeImpl(DelphiLexer.TkPrimaryExpression));
    expression.addChild(integerLiteral);

    return expression;
  }

  private static ExpressionNode procedural(Type returnType) {
    return genericExpression(
        TYPE_FACTORY.createProcedural(
            ProceduralKind.ROUTINE, Collections.emptyList(), returnType, Collections.emptySet()));
  }

  private static ExpressionNode genericExpression(IntrinsicType type) {
    return genericExpression(intrinsicType(type));
  }

  private static ExpressionNode genericExpression(Type type) {
    var primaryExpression = makeSpy(new PrimaryExpressionNodeImpl(DelphiLexer.TkPrimaryExpression));
    doReturn(type).when(primaryExpression).getType();
    doReturn("<expression of type " + type.getImage() + ">").when(primaryExpression).getImage();
    return primaryExpression;
  }

  private static ExpressionNode parenthesize(ExpressionNode expression) {
    var parenthesized = new ParenthesizedExpressionNodeImpl(DelphiLexer.TkNestedExpression);
    parenthesized.addChild(commonDelphiNode(DelphiLexer.PAREN_BRACKET_LEFT, "("));
    parenthesized.addChild(expression);
    parenthesized.addChild(commonDelphiNode(DelphiLexer.PAREN_BRACKET_RIGHT, ")"));
    return parenthesized;
  }

  @SuppressWarnings("ObjectToString")
  private static <T extends DelphiNodeImpl> T makeSpy(T spied) {
    final T result = spy(spied);

    doReturn(TYPE_FACTORY).when(result).getTypeFactory();
    // This is just to improve the display string in parameterized tests
    doAnswer(invocation -> result.getImage()).when(result).toString();

    return result;
  }

  private static CommonDelphiNode commonDelphiNode(int type, String text) {
    return new CommonDelphiNodeImpl(new CommonToken(type, text));
  }

  private static void assertInferred(ExpressionNode expression, Type type) {
    doAssertInferred(expression, type);
    if (expression != null) {
      doAssertInferred(parenthesize(expression), type);
    }
  }

  private static void doAssertInferred(ExpressionNode expression, Type type) {
    TypeInferrer inferrer = new TypeInferrer(TYPE_FACTORY);
    Type inferred = inferrer.infer(expression);

    assertThat(inferred.is(type))
        .withFailMessage(
            String.format(
                "Expected %s to be inferred to %s, but was %s",
                expression == null ? "null" : expression.getImage(),
                type.getImage(),
                inferred.getImage()))
        .isTrue();
  }
}
