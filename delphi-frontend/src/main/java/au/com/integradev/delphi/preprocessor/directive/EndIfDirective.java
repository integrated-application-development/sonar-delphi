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
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

public class EndIfDirective extends ConditionalDirectiveImpl {
  EndIfDirective(DelphiToken token) {
    super(token, ConditionalKind.ENDIF);
  }

  @Override
  public void execute(DelphiPreprocessor preprocessor) {
    // The preprocessor uses this directive as a flag to pop a branching directive off the stack.
    // It doesn't actually do anything at execution time.
  }
}
