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

import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.IfExpressionNode;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class IfExpressionTypeResolverTest {
  private static final TypeFactory FACTORY = TypeFactoryUtils.defaultFactory();
  private static final ExpressionTypeResolver RESOLVER = new ExpressionTypeResolver(FACTORY);

  @Test
  void testBranchTypesAreResolvedInOrder() {
    assertThat(resolve(IntrinsicType.BYTEBOOL, IntrinsicType.BOOLEAN))
        .isEqualTo(FACTORY.getIntrinsic(IntrinsicType.BYTEBOOL));
    assertThat(resolve(IntrinsicType.BOOLEAN, IntrinsicType.BYTEBOOL))
        .isEqualTo(FACTORY.getIntrinsic(IntrinsicType.BOOLEAN));
  }

  private static Type resolve(IntrinsicType then, IntrinsicType els) {
    ExpressionNode thenExpression = mock(ExpressionNode.class);
    ExpressionNode elseExpression = mock(ExpressionNode.class);
    when(thenExpression.getType()).thenReturn(FACTORY.getIntrinsic(then));
    when(elseExpression.getType()).thenReturn(FACTORY.getIntrinsic(els));

    IfExpressionNode node = mock(IfExpressionNode.class);
    when(node.getThenExpression()).thenReturn(thenExpression);
    when(node.getElseExpression()).thenReturn(elseExpression);

    return RESOLVER.resolve(node);
  }
}
