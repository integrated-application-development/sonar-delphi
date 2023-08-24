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
package au.com.integradev.delphi.symbol.scope;

import au.com.integradev.delphi.type.intrinsic.IntrinsicsInjector;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.SystemScope;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public class SystemScopeImpl extends FileScopeImpl implements SystemScope {
  private TypeNameDeclaration objectDeclaration;
  private TypeNameDeclaration interfaceDeclaration;
  private TypeNameDeclaration varRecDeclaration;
  private TypeNameDeclaration classHelperBase;

  public SystemScopeImpl(TypeFactory typeFactory) {
    super("System");
    injectIntrinsics(typeFactory);
  }

  private void injectIntrinsics(TypeFactory typeFactory) {
    IntrinsicsInjector injector = new IntrinsicsInjector(typeFactory);
    injector.inject(this);
  }

  @Override
  public void addDeclaration(NameDeclaration declaration) {
    if (declaration instanceof TypeNameDeclaration) {
      TypeNameDeclaration typeDeclaration = (TypeNameDeclaration) declaration;
      switch (typeDeclaration.getImage()) {
        case "TObject":
          this.objectDeclaration = typeDeclaration;
          break;

        case "IInterface":
          this.interfaceDeclaration = typeDeclaration;
          break;

        case "TVarRec":
          this.varRecDeclaration = typeDeclaration;
          break;

        case "TClassHelperBase":
          this.classHelperBase = typeDeclaration;
          break;

        default:
          // Do nothing
      }
    }

    super.addDeclaration(declaration);
  }

  @Override
  public TypeNameDeclaration getTObjectDeclaration() {
    return objectDeclaration;
  }

  @Override
  public TypeNameDeclaration getIInterfaceDeclaration() {
    return interfaceDeclaration;
  }

  @Override
  public TypeNameDeclaration getTVarRecDeclaration() {
    return varRecDeclaration;
  }

  @Override
  public TypeNameDeclaration getTClassHelperBaseDeclaration() {
    return classHelperBase;
  }

  @Override
  public SystemScope getSystemScope() {
    return this;
  }
}
