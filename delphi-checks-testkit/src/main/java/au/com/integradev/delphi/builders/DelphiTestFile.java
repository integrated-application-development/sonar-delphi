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
import au.com.integradev.delphi.utils.DelphiUtils;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import org.apache.commons.io.FileUtils;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.ImplementationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceSectionNode;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public interface DelphiTestFile {
  default DelphiAst parse() {
    return delphiFile().getAst();
  }

  String getSourceCode();

  InputFile inputFile();

  default InputFile createInputFile(File baseDir, File file) {
    return TestInputFileBuilder.create("moduleKey", baseDir, file)
        .setContents(getSourceCode())
        .setLanguage(Delphi.KEY)
        .setType(InputFile.Type.MAIN)
        .build();
  }

  int[] getOffset(DelphiTestFileBuilderOffset offset);

  default DelphiInputFile delphiFile() {
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
    when(mock.getPreprocessorFactory()).thenReturn(new DelphiPreprocessorFactory(Platform.WINDOWS));
    return mock;
  }

  static DelphiTestFile.ResourceBuilder fromResource(String path) {
    return new ResourceBuilder(DelphiUtils.getResource(path));
  }

  final class ResourceBuilder implements DelphiTestFile {

    private final File resource;

    private ResourceBuilder(File resource) {
      this.resource = resource;
    }

    @Override
    public String getSourceCode() {
      try {
        return FileUtils.readFileToString(resource, UTF_8.name());
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    @Override
    public InputFile inputFile() {
      return createInputFile(resource.getParentFile(), resource);
    }

    @Override
    public int[] getOffset(DelphiTestFileBuilderOffset offset) {
      int sectionOffset;
      switch (offset.getSection()) {
        case Start:
          sectionOffset = 0;
          break;
        case Declaration:
          sectionOffset =
              parse().getFirstDescendantOfType(InterfaceSectionNode.class).getBeginLine();
          break;
        case Implementation:
          sectionOffset =
              parse().getFirstDescendantOfType(ImplementationSectionNode.class).getBeginLine();
          break;
        default:
          throw new UnsupportedOperationException("Unsupported builder offset section");
      }
      return offset.getOffsetLines(sectionOffset);
    }
  }
}
