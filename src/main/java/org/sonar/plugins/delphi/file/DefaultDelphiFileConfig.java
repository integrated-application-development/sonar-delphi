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
package org.sonar.plugins.delphi.file;

import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.preprocessor.search.SearchPath;
import org.sonar.plugins.delphi.type.factory.TypeFactory;

public class DefaultDelphiFileConfig implements DelphiFileConfig {
  private final String encoding;
  private final TypeFactory typeFactory;
  private final SearchPath searchPath;
  private final Set<String> definitions;
  private final boolean skipImplementation;

  DefaultDelphiFileConfig(
      String encoding,
      TypeFactory typeFactory,
      SearchPath searchPath,
      Set<String> definitions,
      boolean skipImplementation) {
    this.encoding = encoding;
    this.typeFactory = typeFactory;
    this.searchPath = searchPath;
    this.definitions = definitions;
    this.skipImplementation = skipImplementation;
  }

  @Nullable
  @Override
  public String getEncoding() {
    return encoding;
  }

  @Override
  public TypeFactory getTypeFactory() {
    return typeFactory;
  }

  @Override
  public SearchPath getSearchPath() {
    return searchPath;
  }

  @Override
  public Set<String> getDefinitions() {
    return definitions;
  }

  @Override
  public boolean shouldSkipImplementation() {
    return skipImplementation;
  }
}
