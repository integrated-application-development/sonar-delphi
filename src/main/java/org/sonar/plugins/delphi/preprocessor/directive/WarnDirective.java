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
package org.sonar.plugins.delphi.preprocessor.directive;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.preprocessor.DelphiPreprocessor;

public class WarnDirective extends AbstractCompilerDirective {
  public enum WarnDirectiveValue {
    ON,
    OFF,
    ERROR,
    DEFAULT,
    UNKNOWN
  }

  private static final Pattern WHITESPACE = Pattern.compile("\\s+");
  private final WarnDirectiveValue value;

  public WarnDirective(Token token, CompilerDirectiveType type, String item) {
    super(token, type);
    String valueString = extractValueString(item);
    this.value =
        Arrays.stream(WarnDirectiveValue.values())
            .filter(wdv -> wdv.name().equalsIgnoreCase(valueString))
            .findFirst()
            .orElse(WarnDirectiveValue.UNKNOWN);
  }

  public WarnDirectiveValue getValue() {
    return value;
  }

  @Override
  public void execute(DelphiPreprocessor preprocessor) {
    // Do nothing
  }

  @Nullable
  private static String extractValueString(String item) {
    return Iterables.get(Splitter.on(WHITESPACE).split(item), 1, null);
  }
}
