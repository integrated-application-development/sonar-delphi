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
package au.com.integradev.delphi.preprocessor.directive;

import static au.com.integradev.delphi.preprocessor.directive.CompilerDirective.Expression.ConstExpressionType.BOOLEAN;

import au.com.integradev.delphi.preprocessor.DelphiPreprocessor;
import au.com.integradev.delphi.preprocessor.directive.CompilerDirective.Expression.ExpressionValue;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

public class IfDirective extends BranchDirective {
  private final CompilerDirective.Expression expression;

  IfDirective(DelphiToken token, CompilerDirectiveType type, CompilerDirective.Expression expression) {
    super(token, type);
    this.expression = expression;
  }

  @Override
  boolean isSuccessfulBranch(DelphiPreprocessor preprocessor) {
    ExpressionValue value = expression.evaluate(preprocessor);
    return value.type() == BOOLEAN && value.asBoolean();
  }
}
