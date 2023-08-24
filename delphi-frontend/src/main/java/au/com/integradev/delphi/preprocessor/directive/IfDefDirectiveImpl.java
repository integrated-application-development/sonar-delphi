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
package au.com.integradev.delphi.preprocessor.directive;

import au.com.integradev.delphi.preprocessor.DelphiPreprocessor;
import org.sonar.plugins.communitydelphi.api.directive.IfDefDirective;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

class IfDefDirectiveImpl extends BranchDirective implements IfDefDirective {
  private final String symbol;

  IfDefDirectiveImpl(DelphiToken token, String symbol) {
    super(token, ConditionalKind.IFDEF);
    this.symbol = symbol;
  }

  @Override
  public boolean isSuccessfulBranch(DelphiPreprocessor preprocessor) {
    return preprocessor.isDefined(symbol);
  }

  @Override
  public String getSymbol() {
    return symbol;
  }
}
