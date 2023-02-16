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
package au.com.integradev.delphi.symbol.scope;

import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.MethodScope;

public class MethodScopeImpl extends DelphiScopeImpl implements MethodScope {
  private MethodNameDeclaration methodNameDeclaration;
  private DelphiScope typeScope;

  @Override
  @Nullable
  public DelphiScope getTypeScope() {
    return typeScope;
  }

  @Override
  @Nullable
  public MethodNameDeclaration getMethodNameDeclaration() {
    return methodNameDeclaration;
  }

  public void setTypeScope(DelphiScope typeScope) {
    this.typeScope = typeScope;
  }

  public void setMethodNameDeclaration(MethodNameDeclaration methodNameDeclaration) {
    this.methodNameDeclaration = methodNameDeclaration;
  }

  @Override
  public String toString() {
    String result = "<MethodScope>";
    if (methodNameDeclaration != null) {
      result = methodNameDeclaration.getImage() + " " + result;
    }
    return result;
  }
}
