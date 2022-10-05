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

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.preprocessor.DelphiPreprocessor;

public class UndefineDirective extends DefaultCompilerDirective {
  private final String define;

  UndefineDirective(Token token, CompilerDirectiveType type, String define) {
    super(token, type);
    this.define = define;
  }

  @Override
  public void execute(DelphiPreprocessor preprocessor) {
    preprocessor.undefine(define);
  }
}
