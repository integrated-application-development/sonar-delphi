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

import org.sonar.plugins.communitydelphi.api.symbol.scope.SysInitScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.SystemScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.UnitScope;

public class UnitScopeImpl extends FileScopeImpl implements UnitScope {
  private final SystemScope systemScope;

  public UnitScopeImpl(String name, SystemScope systemScope, SysInitScope sysInitScope) {
    super(name);
    this.systemScope = systemScope;
    addImport(systemScope);
    addImport(sysInitScope);
  }

  @Override
  public SystemScope getSystemScope() {
    return systemScope;
  }

  @Override
  public String toString() {
    return getName() + "<UnitScope>";
  }
}
