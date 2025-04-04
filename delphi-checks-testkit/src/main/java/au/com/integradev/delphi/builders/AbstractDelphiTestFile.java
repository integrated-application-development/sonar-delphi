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
package au.com.integradev.delphi.builders;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.core.Delphi;
import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import au.com.integradev.delphi.file.DelphiFileConfig;
import au.com.integradev.delphi.preprocessor.DelphiPreprocessorFactory;
import au.com.integradev.delphi.preprocessor.search.SearchPath;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Collections;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

abstract class AbstractDelphiTestFile<T extends AbstractDelphiTestFile<T>>
    implements DelphiTestFile {
  protected abstract T getThis();

  protected abstract String getFileName();

  protected InputFile inputFile() {
    try {
      File baseDir = Files.createTempDirectory("baseDir").toFile();
      baseDir.deleteOnExit();

      File file = baseDir.toPath().resolve(getFileName()).toFile();
      file.deleteOnExit();

      try (FileWriter fileWriter = new FileWriter(file, UTF_8)) {
        // Prepend UTF-8 BOM
        fileWriter.write('\ufeff');
        fileWriter.write(sourceCode());
        fileWriter.flush();
      }

      return createInputFile(baseDir, file);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  protected InputFile createInputFile(File baseDir, File file) {
    return TestInputFileBuilder.create("moduleKey", baseDir, file)
        .setContents(sourceCode())
        .setLanguage(Delphi.KEY)
        .setType(InputFile.Type.MAIN)
        .build();
  }

  @Override
  public DelphiInputFile delphiFile() {
    return DelphiInputFile.from(inputFile(), mockConfig());
  }

  private static DelphiFileConfig mockConfig() {
    TypeFactory typeFactory =
        new TypeFactoryImpl(
            DelphiProperties.COMPILER_TOOLCHAIN_DEFAULT, DelphiProperties.COMPILER_VERSION_DEFAULT);
    DelphiFileConfig mock = mock(DelphiFileConfig.class);
    when(mock.getEncoding()).thenReturn(UTF_8.name());
    when(mock.getTypeFactory()).thenReturn(typeFactory);
    when(mock.getSearchPath()).thenReturn(SearchPath.create(Collections.emptyList()));
    when(mock.getDefinitions()).thenReturn(Collections.emptySet());
    when(mock.getPreprocessorFactory())
        .thenReturn(new DelphiPreprocessorFactory(mock(), Platform.WINDOWS));
    return mock;
  }
}
