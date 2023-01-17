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
package au.com.integradev.delphi.msbuild.condition;

import au.com.integradev.delphi.msbuild.ProjectProperties;
import java.nio.file.Path;
import java.util.List;

public final class ConditionEvaluator {
  private final ExpressionEvaluator expressionEvaluator;

  public ConditionEvaluator(ProjectProperties properties, Path evaluationDirectory) {
    expressionEvaluator = new ExpressionEvaluator(evaluationDirectory, properties.substitutor());
  }

  public boolean evaluate(String condition) {
    if (condition == null) {
      return true;
    }

    if (condition.isEmpty()) {
      return false;
    }

    try {
      ConditionLexer lexer = new ConditionLexer();
      List<Token> tokens = lexer.lex(condition);

      ConditionParser parser = new ConditionParser();
      Expression expression = parser.parse(tokens);

      return expression
          .boolEvaluate(expressionEvaluator)
          .orElseThrow(() -> new ConditionDoesNotEvaluateToBooleanException(condition));
    } catch (Exception e) {
      throw new ConditionEvaluationError(condition, e);
    }
  }

  private static class ConditionDoesNotEvaluateToBooleanException extends RuntimeException {
    ConditionDoesNotEvaluateToBooleanException(String condition) {
      super(String.format("Specified condition \"%s\" does not evaluate to boolean.", condition));
    }
  }
}
