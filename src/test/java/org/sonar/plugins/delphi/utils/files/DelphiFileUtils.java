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
package org.sonar.plugins.delphi.utils.files;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.sonar.plugins.delphi.file.DelphiFileConfig;
import org.sonar.plugins.delphi.preprocessor.search.SearchPath;
import org.sonar.plugins.delphi.utils.types.TypeFactoryUtils;

public final class DelphiFileUtils {
  private DelphiFileUtils() {
    // Utility class
  }

  public static DelphiFileConfig mockConfig() {
    DelphiFileConfig mock = mock(DelphiFileConfig.class);
    when(mock.getEncoding()).thenReturn(StandardCharsets.UTF_8.name());
    when(mock.getTypeFactory()).thenReturn(TypeFactoryUtils.defaultFactory());
    when(mock.getSearchPath()).thenReturn(SearchPath.create(Collections.emptyList()));
    when(mock.getDefinitions()).thenReturn(Collections.emptySet());
    return mock;
  }
}
