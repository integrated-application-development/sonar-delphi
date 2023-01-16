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
package org.sonar.plugins.communitydelphi.symbol.scope;

import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.communitydelphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.type.factory.TypeFactory;
import org.sonar.plugins.communitydelphi.type.intrinsic.IntrinsicsInjector;

public class SystemScope extends AbstractFileScope {
  private TypeNameDeclaration objectDeclaration;
  private TypeNameDeclaration interfaceDeclaration;
  private TypeNameDeclaration varRecDeclaration;
  private TypeNameDeclaration classHelperBase;

  public SystemScope(TypeFactory typeFactory) {
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

  public TypeNameDeclaration getTObjectDeclaration() {
    return objectDeclaration;
  }

  public TypeNameDeclaration getIInterfaceDeclaration() {
    return interfaceDeclaration;
  }

  public TypeNameDeclaration getTVarRecDeclaration() {
    return varRecDeclaration;
  }

  public TypeNameDeclaration getTClassHelperBaseDeclaration() {
    return classHelperBase;
  }

  @Override
  public SystemScope getSystemScope() {
    return this;
  }
}
