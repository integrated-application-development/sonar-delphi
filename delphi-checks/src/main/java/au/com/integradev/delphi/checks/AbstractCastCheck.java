/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.checks;

import au.com.integradev.delphi.utils.CastUtils;
import au.com.integradev.delphi.utils.CastUtils.DelphiCast;
import java.util.Optional;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonar.plugins.communitydelphi.api.type.Type;

public abstract class AbstractCastCheck extends DelphiCheck {
  protected abstract boolean isViolation(Type originalType, Type castType);

  protected abstract String getIssueMessage();

  @Override
  public DelphiCheckContext visit(
      BinaryExpressionNode binaryExpression, DelphiCheckContext context) {
    Optional<DelphiCast> cast = CastUtils.readSoftCast(binaryExpression);

    if (cast.isPresent()) {
      Type originalType = cast.get().originalType();
      Type castedType = cast.get().castedType();

      if (isViolation(originalType, castedType)
          && !originalType.isUnknown()
          && !castedType.isUnknown()) {
        reportIssue(context, binaryExpression, getIssueMessage());
      }
    }

    return super.visit(binaryExpression, context);
  }

  @Override
  public DelphiCheckContext visit(
      PrimaryExpressionNode primaryExpression, DelphiCheckContext context) {
    Optional<DelphiCast> cast = CastUtils.readHardCast(primaryExpression, context.getTypeFactory());

    if (cast.isPresent()) {
      Type originalType = cast.get().originalType();
      Type castedType = cast.get().castedType();

      if (castedType != null && isViolation(originalType, castedType)) {
        DelphiNode name = primaryExpression.getChild(0);
        DelphiNode argumentList = primaryExpression.getChild(1);

        context
            .newIssue()
            .onFilePosition(
                FilePosition.from(
                    name.getBeginLine(),
                    name.getBeginColumn(),
                    argumentList.getEndLine(),
                    argumentList.getEndColumn()))
            .withMessage(getIssueMessage())
            .report();
      }
    }

    return super.visit(primaryExpression, context);
  }
}
