/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package au.com.integradev.delphi;

import au.com.integradev.delphi.checks.CheckList;
import org.sonar.plugins.communitydelphi.api.check.CheckRegistrar;
import org.sonar.plugins.communitydelphi.api.check.MetadataResourcePath;
import org.sonar.plugins.communitydelphi.api.check.ScopeMetadataLoader;

public class DelphiCheckRegistrar implements CheckRegistrar {
  private final MetadataResourcePath metadataResourcePath;

  public DelphiCheckRegistrar(MetadataResourcePath metadataResourcePath) {
    this.metadataResourcePath = metadataResourcePath;
  }

  @Override
  public void register(RegistrarContext registrarContext) {
    ScopeMetadataLoader scopeMetadataLoader =
        new ScopeMetadataLoader(metadataResourcePath, getClass().getClassLoader());

    registrarContext.registerClassesForRepository(
        CheckList.REPOSITORY_KEY, CheckList.getChecks(), scopeMetadataLoader::getScope);
  }
}
