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

import au.com.integradev.delphi.preprocessor.DelphiPreprocessor;
import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.Token;

public abstract class BranchDirective extends AbstractCompilerDirective {
  private final List<CompilerDirective> directives;
  private final List<Token> tokens;

  BranchDirective(Token token, CompilerDirectiveType type) {
    super(token, type);
    this.directives = new ArrayList<>();
    this.tokens = new ArrayList<>();
  }

  List<CompilerDirective> getDirectives() {
    return directives;
  }

  List<Token> getTokens() {
    return tokens;
  }

  void addDirective(CompilerDirective directive) {
    directives.add(directive);
  }

  void addToken(Token token) {
    tokens.add(token);
  }

  @Override
  public void execute(DelphiPreprocessor preprocessor) {
    directives.forEach(directive -> directive.execute(preprocessor));
  }

  abstract boolean isSuccessfulBranch(DelphiPreprocessor preprocessor);
}
