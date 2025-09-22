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
package au.com.integradev.delphi.utils.files;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.file.DelphiFile;
import au.com.integradev.delphi.file.DelphiFileConfig;
import au.com.integradev.delphi.preprocessor.DelphiPreprocessorFactory;
import au.com.integradev.delphi.preprocessor.search.SearchPath;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for creating and configuring Delphi files for testing purposes.
 *
 * <p>This class provides convenient methods for parsing Delphi source code and creating mock
 * configurations commonly used in unit tests.
 */
public final class DelphiFileUtils {
  private DelphiFileUtils() {
    // Utility class
  }

  /**
   * Parses the given lines of Delphi source code into a DelphiFile.
   *
   * <p>This method creates a temporary file with the provided content and parses it using a mock
   * configuration. The temporary file is automatically deleted when the JVM exits.
   *
   * @param lines the lines of Delphi source code to parse
   * @return a parsed DelphiFile instance
   * @throws UncheckedIOException if an I/O error occurs during file creation or writing
   */
  public static DelphiFile parse(String... lines) {
    try {
      Path path = Files.createTempFile("delphi-test-", ".pas");
      Files.writeString(path, "\uFEFF" + StringUtils.join(lines, '\n'), StandardCharsets.UTF_8);
      path.toFile().deleteOnExit();
      return DelphiFile.from(path.toFile(), mockConfig());
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to create temporary Delphi file for parsing", e);
    }
  }

  /**
   * Creates a mock DelphiFileConfig with default settings suitable for testing.
   *
   * <p>The mock configuration includes:
   *
   * <ul>
   *   <li>UTF-8 encoding
   *   <li>Default compiler version and Windows platform
   *   <li>Default type factory
   *   <li>Empty search path and definitions
   * </ul>
   *
   * @return a mock DelphiFileConfig instance
   */
  public static DelphiFileConfig mockConfig() {
    DelphiFileConfig mock = mock(DelphiFileConfig.class);
    when(mock.getEncoding()).thenReturn(StandardCharsets.UTF_8.name());
    when(mock.getPreprocessorFactory())
        .thenReturn(
            new DelphiPreprocessorFactory(
                DelphiProperties.COMPILER_VERSION_DEFAULT, Platform.WINDOWS));
    when(mock.getTypeFactory()).thenReturn(TypeFactoryUtils.defaultFactory());
    when(mock.getSearchPath()).thenReturn(SearchPath.create(Collections.emptyList()));
    when(mock.getDefinitions()).thenReturn(Collections.emptySet());
    return mock;
  }
}
