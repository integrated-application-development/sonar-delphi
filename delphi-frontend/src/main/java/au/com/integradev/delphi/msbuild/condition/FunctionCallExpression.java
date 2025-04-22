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
package au.com.integradev.delphi.msbuild.condition;

import au.com.integradev.delphi.msbuild.expression.ExpressionEvaluator;
import au.com.integradev.delphi.utils.DelphiUtils;
import com.google.common.base.Splitter;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public class FunctionCallExpression implements Expression {
  private final String name;
  private final List<Expression> arguments;

  public FunctionCallExpression(String name, List<Expression> arguments) {
    this.name = name;
    this.arguments = arguments;
  }

  @Override
  public Optional<Boolean> boolEvaluate(ExpressionEvaluator evaluator) {
    if (name.equalsIgnoreCase("Exists")) {
      return Optional.of(exists(evaluator));
    } else if (name.equalsIgnoreCase("HasTrailingSlash")) {
      return Optional.of(hasTrailingSlash(evaluator));
    } else {
      throw new UnknownFunctionException(name);
    }
  }

  private boolean exists(ExpressionEvaluator evaluator) {
    ensureOneArgument();
    Expression argument = arguments.get(0);

    String value = argument.getExpandedValue(evaluator).orElseThrow();
    if (value.isBlank()) {
      return false;
    }

    Path baseDir = evaluator.getState().getThisFilePath().getParent();
    try {
      return Stream.of(StringUtils.split(value, ";"))
          .map(DelphiUtils::normalizeFileName)
          .map(Path::of)
          .map(path -> DelphiUtils.resolvePathFromBaseDir(baseDir, path))
          .allMatch(Files::exists);
    } catch (InvalidPathException e) {
      // MSBuild will silently return false if the path contains invalid characters
      return false;
    }
  }

  private boolean hasTrailingSlash(ExpressionEvaluator evaluator) {
    ensureOneArgument();
    Expression argument = arguments.get(0);

    String value = argument.getExpandedValue(evaluator).orElseThrow();
    List<String> paths = Splitter.on(";").omitEmptyStrings().splitToList(value);

    switch (paths.size()) {
      case 0:
        return false;
      case 1:
        return paths.get(0).endsWith("\\") || paths.get(0).endsWith("/");
      default:
        throw new ScalarFunctionWithMultipleItemsException(name, paths.size());
    }
  }

  private void ensureOneArgument() {
    if (arguments.size() != 1) {
      throw new ArgumentCountMismatchException(name, arguments.size(), 1);
    }
  }

  public static final class ArgumentCountMismatchException extends RuntimeException {
    private ArgumentCountMismatchException(String name, int actualCount, int expectedCount) {
      super(
          String.format("Expected %d arguments for %s, got %d.", expectedCount, name, actualCount));
    }
  }

  public static final class ScalarFunctionWithMultipleItemsException extends RuntimeException {
    private ScalarFunctionWithMultipleItemsException(String name, int itemCount) {
      super(String.format("Scalar function %s can only accept 1 item, got %d", name, itemCount));
    }
  }

  public static final class UnknownFunctionException extends RuntimeException {
    private UnknownFunctionException(String name) {
      super(String.format("Unknown function: %s", name));
    }
  }
}
