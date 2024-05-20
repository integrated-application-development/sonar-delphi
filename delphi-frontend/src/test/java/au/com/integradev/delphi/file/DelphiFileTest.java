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
package au.com.integradev.delphi.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.core.Delphi;
import au.com.integradev.delphi.file.DelphiFile.DelphiFileConstructionException;
import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import au.com.integradev.delphi.file.DelphiFile.EmptyDelphiFileException;
import au.com.integradev.delphi.preprocessor.DelphiPreprocessorFactory;
import au.com.integradev.delphi.preprocessor.search.SearchPath;
import au.com.integradev.delphi.utils.DelphiUtils;
import au.com.integradev.delphi.utils.files.DelphiFileUtils;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;

class DelphiFileTest {
  private static final File BASE_DIR = DelphiUtils.getResource("/au/com/integradev/delphi/file");

  @Test
  void testEmptyFileShouldThrowException() {
    File sourceFile = DelphiUtils.getResource("/au/com/integradev/delphi/file/Empty.pas");
    DelphiFileConfig config = DelphiFileUtils.mockConfig();

    assertThatThrownBy(() -> DelphiFile.from(sourceFile, config))
        .isInstanceOf(DelphiFileConstructionException.class)
        .hasCauseInstanceOf(EmptyDelphiFileException.class);
  }

  @Test
  void testInputFileEncodingShouldOverrideProvidedEncoding() {
    File file = DelphiUtils.getResource("/au/com/integradev/delphi/file/Windows1252.pas");

    InputFile inputFile =
        new TestInputFileBuilder("moduleKey", BASE_DIR, file)
            .setLanguage(Delphi.KEY)
            .setCharset(Charset.forName("windows-1252"))
            .build();

    DelphiFileConfig config =
        DelphiFile.createConfig(
            StandardCharsets.UTF_8.name(),
            new DelphiPreprocessorFactory(Platform.WINDOWS),
            TypeFactoryUtils.defaultFactory(),
            SearchPath.create(Collections.emptyList()),
            Collections.emptySet());

    DelphiFile delphiFile = DelphiInputFile.from(inputFile, config);
    assertThat(delphiFile.getSourceCodeFileLines().get(4)).isEqualTo("// €†šŸÀÿ");
  }

  @Test
  void testByteOrderMarkShouldOverrideProvidedEncoding() {
    File file = DelphiUtils.getResource("/au/com/integradev/delphi/file/Utf16.pas");

    DelphiFileConfig config =
        DelphiFile.createConfig(
            StandardCharsets.UTF_8.name(),
            new DelphiPreprocessorFactory(Platform.WINDOWS),
            TypeFactoryUtils.defaultFactory(),
            SearchPath.create(Collections.emptyList()),
            Collections.emptySet());

    DelphiFile delphiFile = DelphiFile.from(file, config);
    assertThat(delphiFile.getSourceCodeFileLines().get(4)).hasSize(120);
  }
}
